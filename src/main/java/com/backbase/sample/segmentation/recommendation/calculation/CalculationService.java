package com.backbase.sample.segmentation.recommendation.calculation;

import static java.util.Comparator.comparing;

import com.backbase.openapi.usermanager.v2.UserManagementApi;
import com.backbase.openapi.usermanager.v2.model.GetUser;
import com.backbase.openapi.usersegment.v1.model.PatchUsersRequest;
import com.backbase.openapi.usersegment.v1.model.PatchUsersRequest.OpEnum;
import com.backbase.openapi.usersegment.v1.model.SegmentResource;
import com.backbase.openapi.usersegment.v1.model.SegmentResourceOption;
import com.backbase.openapi.usersegment.v1.model.UsersResource;
import com.backbase.sample.segmentation.recommendation.RecommendationEntry;
import com.backbase.sample.segmentation.recommendation.RecommendationReport;
import com.backbase.sample.segmentation.recommendation.RecommendationReportPersistedEvent;
import com.backbase.sample.segmentation.recommendation.RecommendationRepository;
import com.backbase.sample.segmentation.segment.SegmentService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Service
public class CalculationService {

    private static final Logger log = LoggerFactory.getLogger(CalculationService.class);

    private final RecommendationRepository recommendationRepository;

    private final SegmentService segmentService;

    private final UserManagementApi userManagementApi;

    public CalculationService(RecommendationRepository recommendationRepository, SegmentService segmentService,
        UserManagementApi userManagementApi) {
        this.recommendationRepository = recommendationRepository;
        this.segmentService = segmentService;
        this.userManagementApi = userManagementApi;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRecommendationReportPersistedEvent(RecommendationReportPersistedEvent event) {
        log.trace("handleRecommendationReportPersistedEvent(event)");
        RecommendationReport newRecommendationReport = event.getSavedRecommendation();
        Optional<RecommendationReport> previousRecommendationReport = recommendationRepository.findById(
            newRecommendationReport.getPk() - 1);
        if (previousRecommendationReport.isPresent()) {
            processDifference(
                Differences.between(
                    newRecommendationReport.getEntries(), previousRecommendationReport.get().getEntries()
                )
            );
        } else {
            processDifference(
                Differences.between(
                    newRecommendationReport.getEntries(), Collections.emptyList())
            );
        }
    }

    private void processDifference(Differences differences) {
        Map<Category, Map<SubCategory, Map<Action, List<String>>>> grouped = differences.list.stream().collect(
            Collectors.groupingBy(
                difference -> new Category(difference.entry().getCategory()),
                Collectors.groupingBy(
                    difference -> new SubCategory(difference.entry().getSubCategory()),
                    Collectors.groupingBy(
                        Difference::action,
                        Collectors.mapping(
                            difference -> difference.entry().getUserExternalId(),
                            Collectors.toList()
                        )
                    )
                )
            )
        );

        for (var groupedByCategory : grouped.entrySet()) {
            Category category = groupedByCategory.getKey();
            SegmentResource segmentResource = segmentService.findSegmentByName(category.name());
            if (segmentResource == null) {
                log.warn("Can't find configuration for segment with name {}. Ignoring it", category.name());
                continue;
            }
            List<PatchUsersRequest> patchUsersRequests = new ArrayList<>();
            for (var groupedBySubCategory : groupedByCategory.getValue().entrySet()) {
                SubCategory subCategory = groupedBySubCategory.getKey();
                SegmentResourceOption segmentResourceOption = segmentResource.getOptions().stream()
                    .filter(o -> subCategory.name().equals(o.getValue())).findFirst().orElse(null);
                if (segmentResourceOption == null) {
                    log.warn("Can't find configuration for segment option with name {}. Ignoring it",
                        subCategory.name());
                    continue;
                }
                for (var groupedByOperation : groupedBySubCategory.getValue().entrySet()) {
                    Map<String, String> externalToInternalIdMapping = mapIds(groupedByOperation.getValue());
                    PatchUsersRequest patchUsersRequest = new PatchUsersRequest()
                        .op(mapToOp(groupedByOperation.getKey()))
                        .path(segmentResourceOption.getId())
                        .value(new UsersResource().userIds(new ArrayList<>(externalToInternalIdMapping.values())));
                    patchUsersRequests.add(patchUsersRequest);
                }
            }
            log.info("Updating segment: {}, with {}", category.name, patchUsersRequests);
            segmentService.patch(segmentResource.getSegmentId(), patchUsersRequests);
        }
    }

    public Map<String, String> mapIds(Collection<String> externalIds) {
        var getUserList = userManagementApi.getUsersByExternalIdsBulk(externalIds.stream().toList());
        return getUserList.stream()
            .filter(getUser -> StringUtils.hasText(getUser.getId()))
            .collect(Collectors.toMap(GetUser::getExternalId, GetUser::getId, (left, right) -> left));
    }

    private OpEnum mapToOp(Action action) {
        return switch (action) {
            case ADDED ->  OpEnum.ADD;
            case DELETED ->  OpEnum.REMOVE;
        };
    }

    private record Differences(List<Difference> list) {

        private static final Comparator<RecommendationEntry> COMPARATOR = comparing(
            RecommendationEntry::getUserExternalId)
            .thenComparing(RecommendationEntry::getCategory)
            .thenComparing(RecommendationEntry::getSubCategory);

        static Differences between(List<RecommendationEntry> newRecommendations, List<RecommendationEntry> previousRecommendations) {
            final var addedEntries = new TreeSet<>(COMPARATOR);
            addedEntries.addAll(newRecommendations);
            previousRecommendations.forEach(addedEntries::remove);

            final var removedEntries = new TreeSet<>(COMPARATOR);
            removedEntries.addAll(previousRecommendations);
            newRecommendations.forEach(removedEntries::remove);

            final var concat = Stream.concat(
                addedEntries.stream().map(e -> new Difference(Action.ADDED, e)),
                removedEntries.stream().map(e -> new Difference(Action.DELETED, e))
            );

            return new Differences(concat.toList());
        }

    }

    private record Difference(Action action, RecommendationEntry entry) {

    }

    private enum Action {
        ADDED,
        DELETED
    }

    private record Category(String name) {

    }

    private record SubCategory(String name) {

    }

}

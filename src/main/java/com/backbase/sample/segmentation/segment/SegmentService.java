package com.backbase.sample.segmentation.segment;

import com.backbase.openapi.usersegment.v1.SegmentServiceApi;
import com.backbase.openapi.usersegment.v1.model.PatchUsersRequest;
import com.backbase.openapi.usersegment.v1.model.SegmentCreationRequest;
import com.backbase.openapi.usersegment.v1.model.SegmentResource;
import com.backbase.openapi.usersegment.v1.model.SegmentResourceOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SegmentService {

    private static final Logger log = LoggerFactory.getLogger(SegmentService.class);

    private static final String SEGMENT_SOURCE = "demo";

    private final SegmentsProperties segmentsConfiguration;
    private final SegmentServiceApi segmentServiceApi;
    private final SegmentsCache segmentsCache;

    public SegmentService(SegmentsProperties segmentsConfiguration, SegmentServiceApi segmentServiceApi) {
        this.segmentsConfiguration = segmentsConfiguration;
        this.segmentServiceApi = segmentServiceApi;
        this.segmentsCache = new SegmentsCache();
    }

    public SegmentResource upsertSegment(SegmentDefinition segmentDefinition) {
        if (log.isDebugEnabled()) {
            log.debug("Trying to create segment based on definition: {}", segmentDefinition);
        }
        String segmentDefinitionName = segmentDefinition.getName();
        SegmentResource cached = segmentsCache.get(segmentDefinitionName);
        if (cached != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found segment in local cache, returning it {}", cached);
            }
            return cached;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No segment found in cache for definition: {}, creating a new segment", segmentDefinitionName);
            }
            SegmentCreationRequest creationRequest = toSegmentCreationRequest(segmentDefinition);
            SegmentResource created = segmentServiceApi.upsertSegment(creationRequest);
            if (log.isDebugEnabled()) {
                log.debug("Created segment {} will be stored in cache", created);
            }
            return segmentsCache.put(segmentDefinitionName, created);
        }
    }

    public SegmentResource findSegmentByName(String segmentName) {
        if (log.isDebugEnabled()) {
            log.debug("Trying to get segment by name: {}", segmentName);
        }
        SegmentDefinition segmentDefinition = getSegmentDefinition(segmentName);
        if (segmentDefinition == null) {
            log.warn("Try to find segment definition for {} but can't find it", segmentName);
            return null;
        }
        return upsertSegment(segmentDefinition);
    }

    public void patch(UUID segmentUuid, List<PatchUsersRequest> patchSegmentUsers) {
        segmentServiceApi.patchSegmentUsers(segmentUuid, patchSegmentUsers);
    }

    private SegmentDefinition getSegmentDefinition(String segmentDefinitionName) {
        if (log.isDebugEnabled()) {
            log.debug("Trying to find segment definition for: {}", segmentDefinitionName);
        }
        for (SegmentDefinition segmentDefinition : segmentsConfiguration.getDefinitions()) {
            if (segmentDefinitionName.equals(segmentDefinition.getName())) {
                if (log.isDebugEnabled()) {
                    log.debug("Found segment definition for: {}", segmentDefinition);
                }
                return segmentDefinition;
            }
        }
        log.error("Did not find matching segment definition for: {}", segmentDefinitionName);
        return null;
    }

    private SegmentCreationRequest toSegmentCreationRequest(SegmentDefinition segmentDefinition) {
        return new SegmentCreationRequest()
            .source(SEGMENT_SOURCE)
            .name(segmentDefinition.getName())
            .options(
                segmentDefinition.getOptions().stream()
                    .map(this::toOptionCreationRequest)
                    .collect(Collectors.toSet())
            );
    }

    private SegmentResourceOption toOptionCreationRequest(String option) {
        return new SegmentResourceOption().id(toKebabCase(option)).value(option);
    }

    private String toKebabCase(String name) {
        return name.replaceAll("\\s", "-").toLowerCase(Locale.ENGLISH);
    }

    private static class SegmentsCache {

        private final Map<String, SegmentResource> store = new ConcurrentHashMap<>();

        public SegmentResource get(String segmentDefinitionId) {
            SegmentResource segment = store.get(segmentDefinitionId);
            if (log.isDebugEnabled()) {
                log.debug("Cache hit: {} segmentDefinitionId: {}", (segment != null), segmentDefinitionId);
            }
            return segment;
        }

        public SegmentResource put(String segmentDefinitionId, SegmentResource segment) {
            if (log.isDebugEnabled()) {
                log.debug("Cache put: {}", segmentDefinitionId);
            }
            this.store.put(segmentDefinitionId, segment);
            return segment;
        }

    }

}

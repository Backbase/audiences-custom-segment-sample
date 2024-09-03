package com.backbase.sample.segmentation.recommendation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@Table(name = "recommendation")
public class RecommendationReport extends AbstractAggregateRoot<RecommendationReport> {

    @Id
    @GeneratedValue(generator = "recommendationSequenceGenerator")
    @SequenceGenerator(
        name = "recommendationSequenceGenerator",
        sequenceName = "seq_recommendation",
        initialValue = 1,
        allocationSize = 1
    )
    @Column(name = "id")
    private Long pk;

    private LocalDateTime createdAt;

    @OneToMany(
        mappedBy = "recommendationReport",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RecommendationEntry> entries = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        registerEvent(new RecommendationReportPersistedEvent(this));
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long id) {
        this.pk = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RecommendationEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<RecommendationEntry> entries) {
        this.entries = entries;
    }

    public void addPatch(RecommendationEntry entry) {
        entries.add(entry);
        entry.setRecommendation(this);
    }

}

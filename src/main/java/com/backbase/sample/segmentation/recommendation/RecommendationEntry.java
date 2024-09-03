package com.backbase.sample.segmentation.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_entry")
public class RecommendationEntry {

    @Id
    @GeneratedValue(generator = "recommendationEntrySequenceGenerator")
    @SequenceGenerator(
        name = "recommendationEntrySequenceGenerator",
        sequenceName = "seq_recommendation_entry",
        initialValue = 1,
        allocationSize = 50
    )
    @Column(name = "id")
    private Long pk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", referencedColumnName = "id", nullable = false)
    private RecommendationReport recommendationReport;

    @Column(name = "user_external_id")
    private String userExternalId;

    @Column(name = "category")
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public RecommendationReport getRecommendation() {
        return recommendationReport;
    }

    public void setRecommendation(RecommendationReport request) {
        this.recommendationReport = request;
    }

    public String getUserExternalId() {
        return userExternalId;
    }

    public void setUserExternalId(String userExternalId) {
        this.userExternalId = userExternalId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

}

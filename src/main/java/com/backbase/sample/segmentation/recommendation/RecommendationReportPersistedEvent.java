package com.backbase.sample.segmentation.recommendation;

public class RecommendationReportPersistedEvent {

    private final RecommendationReport savedRecommendationReport;

    public RecommendationReportPersistedEvent(RecommendationReport savedRecommendationReport) {
        this.savedRecommendationReport = savedRecommendationReport;
    }

    public RecommendationReport getSavedRecommendation() {
        return savedRecommendationReport;
    }

}

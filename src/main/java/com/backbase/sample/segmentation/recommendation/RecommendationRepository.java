package com.backbase.sample.segmentation.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<RecommendationReport, Long> {

}

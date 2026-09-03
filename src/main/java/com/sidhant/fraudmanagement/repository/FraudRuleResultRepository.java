package com.sidhant.fraudmanagement.repository;

import com.sidhant.fraudmanagement.entity.FraudRuleResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudRuleResultRepository
        extends JpaRepository<FraudRuleResult, Long> {

    List<FraudRuleResult> findByFraudAssessmentId(Long fraudAssessmentId);
}
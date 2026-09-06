package com.sidhant.fraudmanagement.repository;

import com.sidhant.fraudmanagement.entity.FraudAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FraudAssessmentRepository
        extends JpaRepository<FraudAssessment, Long> {

    Optional<FraudAssessment> findByTransaction_Id(Long transactionId);
    
}
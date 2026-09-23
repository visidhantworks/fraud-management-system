package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.response.AdminTransactionResponse;
import com.sidhant.fraudmanagement.entity.FraudAssessment;
import com.sidhant.fraudmanagement.entity.FraudRuleResult;
import com.sidhant.fraudmanagement.entity.Transaction;
import com.sidhant.fraudmanagement.repository.FraudAssessmentRepository;
import com.sidhant.fraudmanagement.repository.FraudRuleResultRepository;
import com.sidhant.fraudmanagement.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminMonitoringService {

    private final TransactionRepository transactionRepository;

    private final FraudAssessmentRepository fraudAssessmentRepository;

    private final FraudRuleResultRepository fraudRuleResultRepository;

    public AdminMonitoringService(

            TransactionRepository transactionRepository,

            FraudAssessmentRepository fraudAssessmentRepository,

            FraudRuleResultRepository fraudRuleResultRepository

    ) {

        this.transactionRepository = transactionRepository;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudRuleResultRepository = fraudRuleResultRepository;

    }

    public List<AdminTransactionResponse> getAllTransactions() {

        List<Transaction> transactions =
                transactionRepository.findAllByOrderByCreatedAtDesc();

        return transactions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private AdminTransactionResponse convertToResponse(
            Transaction transaction
    ) {

        FraudAssessment assessment =
                fraudAssessmentRepository
                        .findByTransaction_Id(transaction.getId())
                        .orElse(null);

        Integer riskScore = null;
        String triggeredRules = null;

        Double springRiskScore = null;
        Double pythonBehavioralRisk = null;
        Double combinedRisk = null;

        Double averageAmount = null;
        Double maximumAmount = null;
        Double minimumAmount = null;

        Integer transactionCount = null;
        Integer successfulTransactions = null;
        Integer failedTransactions = null;

        Double failedTransactionRatio = null;
        Double amountDeviationPercentage = null;

        Double amountBehaviorRisk = null;
        Double failureBehaviorRisk = null;
        Double frequencyBehaviorRisk = null;
        Double locationBehaviorRisk = null;

        String behaviorCategory = null;

        if (assessment != null) {

            riskScore = assessment.getRiskScore();

            triggeredRules =
                    fraudRuleResultRepository
                            .findByFraudAssessmentId(assessment.getId())
                            .stream()
                            .map(FraudRuleResult::getRuleCode)
                            .reduce(
                                    (first, second) -> first + ", " + second
                            )
                            .orElse(null);

            // Spring + Python risk values
            springRiskScore = assessment.getSpringRiskScore();

            pythonBehavioralRisk =
                    assessment.getPythonBehavioralRisk();

            combinedRisk =
                    assessment.getCombinedRisk();

            // Detailed Python behavioral analysis
            averageAmount =
                    assessment.getAverageAmount();

            maximumAmount =
                    assessment.getMaximumAmount();

            minimumAmount =
                    assessment.getMinimumAmount();

            transactionCount =
                    assessment.getTransactionCount();

            successfulTransactions =
                    assessment.getSuccessfulTransactions();

            failedTransactions =
                    assessment.getFailedTransactions();

            failedTransactionRatio =
                    assessment.getFailedTransactionRatio();

            amountDeviationPercentage =
                    assessment.getAmountDeviationPercentage();

            amountBehaviorRisk =
                    assessment.getAmountBehaviorRisk();

            failureBehaviorRisk =
                    assessment.getFailureBehaviorRisk();

            frequencyBehaviorRisk =
                    assessment.getFrequencyBehaviorRisk();

            locationBehaviorRisk =
                    assessment.getLocationBehaviorRisk();

            behaviorCategory =
                    assessment.getBehaviorCategory();
        }

        return new AdminTransactionResponse(

                transaction.getTransactionId(),

                transaction.getUser().getId(),

                transaction.getUser().getName(),

                transaction.getUser().getEmail(),

                transaction.getAmount(),

                transaction.getLatitude(),

                transaction.getLongitude(),

                riskScore,

                triggeredRules,

                transaction.getStatus(),

                transaction.getCreatedAt(),

                springRiskScore,

                pythonBehavioralRisk,

                combinedRisk,

                averageAmount,

                maximumAmount,

                minimumAmount,

                transactionCount,

                successfulTransactions,

                failedTransactions,

                failedTransactionRatio,

                amountDeviationPercentage,

                amountBehaviorRisk,

                failureBehaviorRisk,

                frequencyBehaviorRisk,

                locationBehaviorRisk,

                behaviorCategory
        );
    }
}
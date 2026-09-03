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

        if (assessment != null) {

            riskScore = assessment.getRiskScore();

            List<FraudRuleResult> ruleResults =
                    fraudRuleResultRepository
                            .findByFraudAssessmentId(assessment.getId());

            triggeredRules = ruleResults.stream()
                    .map(FraudRuleResult::getRuleCode)
                    .reduce((first, second) -> first + ", " + second)
                    .orElse(null);
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
                transaction.getCreatedAt()
        );
    }
}
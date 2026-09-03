package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.request.PaymentRequest;
import com.sidhant.fraudmanagement.dto.response.TransactionResponse;

import com.sidhant.fraudmanagement.entity.FraudAssessment;
import com.sidhant.fraudmanagement.entity.FraudRuleResult;
import com.sidhant.fraudmanagement.entity.Transaction;
import com.sidhant.fraudmanagement.entity.User;

import com.sidhant.fraudmanagement.enums.TransactionStatus;
import com.sidhant.fraudmanagement.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sidhant.fraudmanagement.exception.AdminPaymentException;
import com.sidhant.fraudmanagement.exception.InvalidPinException;
import com.sidhant.fraudmanagement.exception.TransactionBlockedException;
import com.sidhant.fraudmanagement.exception.TransactionFailedException;
import com.sidhant.fraudmanagement.exception.UserNotFoundException;

import com.sidhant.fraudmanagement.repository.FraudAssessmentRepository;
import com.sidhant.fraudmanagement.repository.FraudRuleResultRepository;
import com.sidhant.fraudmanagement.repository.TransactionRepository;
import com.sidhant.fraudmanagement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final FraudRiskService fraudRiskService;
    private final FraudAssessmentRepository fraudAssessmentRepository;
    private final FraudRuleResultRepository fraudRuleResultRepository;

    public TransactionService(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder,
            FraudRiskService fraudRiskService,
            FraudAssessmentRepository fraudAssessmentRepository,
            FraudRuleResultRepository fraudRuleResultRepository
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.fraudRiskService = fraudRiskService;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudRuleResultRepository = fraudRuleResultRepository;
    }

    private void saveFraudRuleResult(FraudAssessment fraudAssessment,String ruleCode,int riskPoints) {

        if (riskPoints <= 0) {
            return;
        }

        FraudRuleResult result = new FraudRuleResult();

        result.setFraudAssessment(fraudAssessment);
        result.setRiskPoints(riskPoints);
        result.setRuleCode(ruleCode);
        result.setTriggered(true);
        result.setCreatedAt(LocalDateTime.now());

        fraudRuleResultRepository.save(result);
    }

    public TransactionResponse makePayment(PaymentRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        if (user.getRole() != UserRole.USER) {
            throw new AdminPaymentException(
                    "Admin cannot make payments"
            );
        }

        boolean pinValid = passwordEncoder.matches(
                request.getPin(),
                user.getPinHash()
        );

         

        int amountRisk =
                fraudRiskService.calculateAmountRisk(
                        request.getAmount()
                );

        System.out.println(
                "Calculated Amount Risk Score: " + amountRisk
        );

        int frequencyRisk =
                fraudRiskService.calculateFrequencyRisk(
                        user.getId()
                );

        System.out.println(
                "Calculated Frequency Risk Score: " + frequencyRisk
        );

        int locationRisk =
                fraudRiskService.calculateLocationRisk(
                        user.getId(),
                        request.getLatitude(),
                        request.getLongitude()
                );

        System.out.println(
                "Calculated Location Risk: " + locationRisk
        );

        int failedAttemptRisk =
                fraudRiskService.calculateFailedAttemptRisk(
                        user.getId(), !pinValid
                );

        System.out.println(
                "Calculated Failed Attempt Risk Score: "
                        + failedAttemptRisk
        );

        int totalRisk =
                amountRisk
                        + frequencyRisk
                        + locationRisk
                        + failedAttemptRisk;

        System.out.println(
                "Total Risk: " + totalRisk
        );

        
        TransactionStatus status;

        if (totalRisk >= 100) {
            status = TransactionStatus.BLOCKED;
        } else if (!pinValid || totalRisk >= 75) {
            status = TransactionStatus.FAILED;
        } else {
            status = TransactionStatus.SUCCESS;
        }

        Transaction transaction = new Transaction();

        transaction.setTransactionId(
                "TXN-" + UUID.randomUUID()
        );

        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setLatitude(request.getLatitude());
        transaction.setLongitude(request.getLongitude());
        transaction.setStatus(status);
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction =
        transactionRepository.save(transaction);

        FraudAssessment assessment = new FraudAssessment();

        assessment.setTransaction(savedTransaction);
        assessment.setRiskScore(totalRisk);
        assessment.setDecision(status.name());
        assessment.setCreatedAt(LocalDateTime.now());

        FraudAssessment savedAssessment =
                fraudAssessmentRepository.save(assessment);
        saveFraudRuleResult(
        savedAssessment,
        "HIGH_TRANSACTION_AMOUNT",
        amountRisk
        );

        saveFraudRuleResult(
                savedAssessment,
                "HIGH_TRANSACTION_FREQUENCY",
                frequencyRisk
        );

        saveFraudRuleResult(
                savedAssessment,
                "UNUSUAL_LOCATION",
                locationRisk
        );

        saveFraudRuleResult(
                savedAssessment,
                "HIGH_FAILED_ATTEMPTS",
                failedAttemptRisk
        );
         if (!pinValid) {

            if (totalRisk >= 100) {
                throw new TransactionBlockedException(
                        "Transaction blocked due to high fraud risk"
                );
            }

            if (totalRisk >= 75) {
                throw new TransactionFailedException(
                        "Transaction failed due to high fraud risk"
                );
            }

            throw new InvalidPinException("Invalid PIN");
        }


        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getUser().getId(),
                savedTransaction.getAmount(),
                savedTransaction.getLatitude(),
                savedTransaction.getLongitude(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt()
        );
    }
    public List<TransactionResponse> getMyTransactions() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new UserNotFoundException("User not found"));

    List<Transaction> transactions =
            transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

    return transactions.stream()
            .map(transaction -> new TransactionResponse(
                    transaction.getTransactionId(),
                    user.getId(),
                    transaction.getAmount(),
                    transaction.getLatitude(),
                    transaction.getLongitude(),
                    transaction.getStatus(),
                    transaction.getCreatedAt()
            ))
            .toList();
    }
}
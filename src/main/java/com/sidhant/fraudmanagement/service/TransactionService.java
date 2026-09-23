package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.request.PaymentRequest;
import com.sidhant.fraudmanagement.dto.request.PythonFraudAnalysisRequest;
import com.sidhant.fraudmanagement.dto.request.PythonTransactionData;
import com.sidhant.fraudmanagement.dto.request.SecurityChallengeRequest;
import com.sidhant.fraudmanagement.dto.response.PythonFraudAnalysisResponse;
import com.sidhant.fraudmanagement.dto.response.TransactionResponse;
import com.sidhant.fraudmanagement.entity.ActiveSession;
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
import com.sidhant.fraudmanagement.repository.ActiveSessionRepository;
import com.sidhant.fraudmanagement.repository.FraudAssessmentRepository;
import com.sidhant.fraudmanagement.repository.FraudRuleResultRepository;
import com.sidhant.fraudmanagement.repository.TransactionRepository;
import com.sidhant.fraudmanagement.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

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
    private final ActiveSessionRepository activeSessionRepository;
    private final PythonFraudAnalysisService pythonFraudAnalysisService;

    public TransactionService(
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            PasswordEncoder passwordEncoder,
            FraudRiskService fraudRiskService,
            FraudAssessmentRepository fraudAssessmentRepository,
            FraudRuleResultRepository fraudRuleResultRepository,
            ActiveSessionRepository activeSessionRepository,
            PythonFraudAnalysisService pythonFraudAnalysisService
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.fraudRiskService = fraudRiskService;
        this.fraudAssessmentRepository = fraudAssessmentRepository;
        this.fraudRuleResultRepository = fraudRuleResultRepository;
        this.activeSessionRepository = activeSessionRepository;
        this.pythonFraudAnalysisService = pythonFraudAnalysisService;
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
    @Transactional(noRollbackFor = {TransactionBlockedException.class , TransactionFailedException.class , InvalidPinException.class})
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
        if(user.getTransactionLockedUntil() != null && LocalDateTime.now().isBefore(user.getTransactionLockedUntil())){
                ActiveSession activeSession = activeSessionRepository.findByUser_IdAndActiveTrue(user.getId()).orElse(null);
                if(activeSession != null){
                        activeSession.setActive(false);
                        activeSession.setLogoutAt(LocalDateTime.now());
                        activeSessionRepository.save(activeSession);
                }
                throw new TransactionBlockedException("Transactions are temporarily restricted due to unusual activity.");
        }
        else{
                user.setTransactionLockedUntil(null);
                userRepository.save(user);
        }




        boolean pinValid = passwordEncoder.matches(
                request.getPin(),
                user.getPinHash()
        );
        List<Transaction> transactionHistory = transactionRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId());
        System.out.println("===== TRANSACTION HISTORY DEBUG =====");
        System.out.println("User ID: " + user.getId());
        System.out.println("History Size: " + transactionHistory.size());

        for (Transaction transaction : transactionHistory) {
        System.out.println(
                transaction.getId()
                + " | "
                + transaction.getAmount()
                + " | "
                + transaction.getStatus()
                + " | "
                + transaction.getCreatedAt()
        );
        }
        PythonTransactionData currentTransaction = new PythonTransactionData(
                        request.getAmount(),
                        "SUCCESS",
                        request.getLatitude(),
                        request.getLongitude(),
                        LocalDateTime.now()
        );
        List<PythonTransactionData> history =transactionHistory.stream().map(transaction -> new PythonTransactionData(
                                                transaction.getAmount(),
                                                transaction.getStatus().name(),
                                                transaction.getLatitude(),
                                                transaction.getLongitude(),
                                                transaction.getCreatedAt()
        )).toList();
        PythonFraudAnalysisRequest pythonRequest = new PythonFraudAnalysisRequest(user.getId() , currentTransaction , history);
        PythonFraudAnalysisResponse pythonResponse = pythonFraudAnalysisService.analyze(pythonRequest);
        double pythonBehaviorRisk = pythonResponse.getBehavioralRisk();
        System.out.println("===== PYTHON REQUEST DEBUG =====");
        System.out.println("User ID: " + pythonRequest.getUserId());
        System.out.println("History Size: " + pythonRequest.getTransactionHistory().size());

        for (PythonTransactionData transaction : pythonRequest.getTransactionHistory()) {
        System.out.println(
                transaction.getAmount()
                + " | "
                + transaction.getStatus()
                + " | "
                + transaction.getCreatedAt()
        );
        }

        System.out.println("===== PYTHON FRAUD ANALYSIS =====");
        System.out.println("Average Amount: "
                + pythonResponse.getAverageAmount());

        System.out.println("Maximum Amount: "
                + pythonResponse.getMaximumAmount());

        System.out.println("Minimum Amount: "
                + pythonResponse.getMinimumAmount());

        System.out.println("Transaction Count: "
                + pythonResponse.getTransactionCount());

        System.out.println("Successful Transactions: "
                + pythonResponse.getSuccessfulTransactions());

        System.out.println("Failed Transactions: "
                + pythonResponse.getFailedTransactions());

        System.out.println("Failed Transaction Ratio: "
                + pythonResponse.getFailedTransactionRatio());

        System.out.println("Amount Deviation: "
                + pythonResponse.getAmountDeviationPercentage()
                + "%");

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
        double combinedRisk = totalRisk + pythonBehaviorRisk;
        System.out.println("===== COMBINED FRAUD RISK =====");

        System.out.println("Spring FRM Risk: " + totalRisk );
        System.out.println("Python Behavorial Risk:" + pythonBehaviorRisk);
        System.out.println("Combined Risk:"+combinedRisk);
        System.out.println("Python Behavior Category:"+pythonResponse.getBehaviorCategory());


        TransactionStatus status;

        if (combinedRisk >= 200) {
        status = TransactionStatus.BLOCKED;
        } else if (!pinValid) {
        status = TransactionStatus.FAILED;
        } else if (combinedRisk >= 175) {
        status = TransactionStatus.SECURITY_CHALLENGE_REQUIRED;
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
        assessment.setSpringRiskScore((double) totalRisk);
        assessment.setPythonBehavioralRisk(pythonBehaviorRisk);
        assessment.setCombinedRisk(combinedRisk);

        // Save detailed Python behavioral analysis
        assessment.setAverageAmount(pythonResponse.getAverageAmount());

        assessment.setMaximumAmount(pythonResponse.getMaximumAmount());

        assessment.setMinimumAmount(pythonResponse.getMinimumAmount());

        assessment.setTransactionCount(pythonResponse.getTransactionCount());

        assessment.setSuccessfulTransactions(pythonResponse.getSuccessfulTransactions());

        assessment.setFailedTransactions(pythonResponse.getFailedTransactions());

        assessment.setFailedTransactionRatio(pythonResponse.getFailedTransactionRatio());

        assessment.setAmountDeviationPercentage(pythonResponse.getAmountDeviationPercentage());

        assessment.setAmountBehaviorRisk(pythonResponse.getAmountBehaviorRisk());

        assessment.setFailureBehaviorRisk(pythonResponse.getFailureBehaviorRisk());

        assessment.setFrequencyBehaviorRisk(pythonResponse.getFrequencyBehaviorRisk());

        assessment.setLocationBehaviorRisk(pythonResponse.getLocationBehaviorRisk());

        assessment.setBehaviorCategory(pythonResponse.getBehaviorCategory());
        assessment.setSecurityChallengeRequired(status == TransactionStatus.SECURITY_CHALLENGE_REQUIRED);
        assessment.setSecurityChallengePassed(false);

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
        if (combinedRisk >= 200) {

        user.setTransactionLockedUntil(LocalDateTime.now().plusMinutes(20));
        userRepository.save(user);
        ActiveSession activeSession = activeSessionRepository
                .findByUser_IdAndActiveTrue(user.getId())
                .orElse(null);

        if (activeSession != null) {
                activeSession.setActive(false);
                activeSession.setLogoutAt(LocalDateTime.now());
                activeSessionRepository.save(activeSession);
        }

        throw new TransactionBlockedException(
                "Transaction blocked due to security checks. Please sign in again."
        );
        }



        if (!pinValid) {
        throw new InvalidPinException("Invalid PIN");
        }

        if (status == TransactionStatus.SECURITY_CHALLENGE_REQUIRED) {
        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getUser().getId(),
                savedTransaction.getAmount(),
                savedTransaction.getLatitude(),
                savedTransaction.getLongitude(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt(),
                user.getSecurityQuestion(),
                "Additional authentication required."
        );
        }

        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getUser().getId(),
                savedTransaction.getAmount(),
                savedTransaction.getLatitude(),
                savedTransaction.getLongitude(),
                savedTransaction.getStatus(),
                savedTransaction.getCreatedAt(),
                null,
                "Payment successful."
        );
}
@Transactional
public TransactionResponse verifySecurityChallenge(SecurityChallengeRequest request) {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    String email = authentication.getName();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new UserNotFoundException("User not found"));

    Transaction transaction =
            transactionRepository.findByTransactionIdAndUserId(
                    request.getTransactionId(),
                    user.getId()
            )
            .orElseThrow(() ->
                    new TransactionFailedException(
                            "Transaction not found"
                    ));

    if (transaction.getStatus()
            != TransactionStatus.SECURITY_CHALLENGE_REQUIRED) {

        throw new TransactionFailedException(
                "Security challenge is not required for this transaction"
        );
    }

    FraudAssessment assessment =
            fraudAssessmentRepository.findByTransaction_Id(
                    transaction.getId()
            )
            .orElseThrow(() ->
                    new TransactionFailedException(
                            "Fraud assessment not found"
                    ));

    boolean answerCorrect =
            passwordEncoder.matches(
                    request.getAnswer(),
                    user.getSecurityAnswerHash()
            );

    if (answerCorrect) {

        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        assessment.setSecurityChallengePassed(true);
        assessment.setDecision(TransactionStatus.SUCCESS.name());
        fraudAssessmentRepository.save(assessment);

        return new TransactionResponse(
                transaction.getTransactionId(),
                user.getId(),
                transaction.getAmount(),
                transaction.getLatitude(),
                transaction.getLongitude(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                null,
                "Security verification successful. Payment approved."
        );
    }

    transaction.setStatus(TransactionStatus.FAILED);
    transactionRepository.save(transaction);

    assessment.setSecurityChallengePassed(false);
    assessment.setDecision(TransactionStatus.FAILED.name());
    fraudAssessmentRepository.save(assessment);

    return new TransactionResponse(
            transaction.getTransactionId(),
            user.getId(),
            transaction.getAmount(),
            transaction.getLatitude(),
            transaction.getLongitude(),
            transaction.getStatus(),
            transaction.getCreatedAt(),
            null,
            "Security verification failed. Payment rejected."
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
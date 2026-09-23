package com.sidhant.fraudmanagement.dto.response;

import com.sidhant.fraudmanagement.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminTransactionResponse {

    private String transactionId;

    private Long userId;

    private String userName;

    private String userEmail;

    private BigDecimal amount;

    private Double latitude;

    private Double longitude;

    private Integer riskScore;

    private String triggeredRules;

    private TransactionStatus status;

    private LocalDateTime createdAt;

    private Double springRiskScore;

    private Double pythonBehavioralRisk;

    private Double combinedRisk;


    private Double averageAmount;

    private Double maximumAmount;

    private Double minimumAmount;

    private Integer transactionCount;

    private Integer successfulTransactions;

    private Integer failedTransactions;

    private Double failedTransactionRatio;

    private Double amountDeviationPercentage;

    private Double amountBehaviorRisk;

    private Double failureBehaviorRisk;

    private Double frequencyBehaviorRisk;

    private Double locationBehaviorRisk;

    private String behaviorCategory;

    public AdminTransactionResponse(

            String transactionId,

            Long userId,

            String userName,

            String userEmail,

            BigDecimal amount,

            Double latitude,

            Double longitude,

            Integer riskScore,

            String triggeredRules,

            TransactionStatus status,

            LocalDateTime createdAt,

            Double springRiskScore,

            Double pythonBehavioralRisk,

            Double combinedRisk,

            Double averageAmount,

            Double maximumAmount,

            Double minimumAmount,

            Integer transactionCount,

            Integer successfulTransactions,

            Integer failedTransactions,

            Double failedTransactionRatio,

            Double amountDeviationPercentage,

            Double amountBehaviorRisk,

            Double failureBehaviorRisk,

            Double frequencyBehaviorRisk,

            Double locationBehaviorRisk,

            String behaviorCategory

    ) {

        this.transactionId = transactionId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.amount = amount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.riskScore = riskScore;
        this.triggeredRules = triggeredRules;
        this.status = status;
        this.createdAt = createdAt;

        this.springRiskScore = springRiskScore;
        this.pythonBehavioralRisk = pythonBehavioralRisk;
        this.combinedRisk = combinedRisk;

        this.averageAmount = averageAmount;
        this.maximumAmount = maximumAmount;
        this.minimumAmount = minimumAmount;
        this.transactionCount = transactionCount;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.failedTransactionRatio = failedTransactionRatio;
        this.amountDeviationPercentage = amountDeviationPercentage;
        this.amountBehaviorRisk = amountBehaviorRisk;
        this.failureBehaviorRisk = failureBehaviorRisk;
        this.frequencyBehaviorRisk = frequencyBehaviorRisk;
        this.locationBehaviorRisk = locationBehaviorRisk;
        this.behaviorCategory = behaviorCategory;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public String getTriggeredRules() {
        return triggeredRules;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getSpringRiskScore() {
        return springRiskScore;
    }

    public Double getPythonBehavioralRisk() {
        return pythonBehavioralRisk;
    }

    public Double getCombinedRisk() {
        return combinedRisk;
    }

    public Double getAverageAmount() {
        return averageAmount;
    }

    public Double getMaximumAmount() {
        return maximumAmount;
    }

    public Double getMinimumAmount() {
        return minimumAmount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public Integer getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public Integer getFailedTransactions() {
        return failedTransactions;
    }

    public Double getFailedTransactionRatio() {
        return failedTransactionRatio;
    }

    public Double getAmountDeviationPercentage() {
        return amountDeviationPercentage;
    }

    public Double getAmountBehaviorRisk() {
        return amountBehaviorRisk;
    }

    public Double getFailureBehaviorRisk() {
        return failureBehaviorRisk;
    }

    public Double getFrequencyBehaviorRisk() {
        return frequencyBehaviorRisk;
    }

    public Double getLocationBehaviorRisk() {
        return locationBehaviorRisk;
    }

    public String getBehaviorCategory() {
        return behaviorCategory;
    }
}
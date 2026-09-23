package com.sidhant.fraudmanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_assessments")
public class FraudAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "decision", nullable = false, length = 50)
    private String decision;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // =========================
    // SPRING FRM RISK
    // =========================

    @Column(name = "spring_risk_score")
    private Double springRiskScore;

    // =========================
    // PYTHON BEHAVIORAL RISK
    // =========================

    @Column(name = "python_behavioral_risk")
    private Double pythonBehavioralRisk;

    @Column(name = "combined_risk")
    private Double combinedRisk;

    // =========================
    // PYTHON BEHAVIOR DETAILS
    // =========================

    @Column(name = "average_amount")
    private Double averageAmount;

    @Column(name = "maximum_amount")
    private Double maximumAmount;

    @Column(name = "minimum_amount")
    private Double minimumAmount;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "successful_transactions")
    private Integer successfulTransactions;

    @Column(name = "failed_transactions")
    private Integer failedTransactions;

    @Column(name = "failed_transaction_ratio")
    private Double failedTransactionRatio;

    @Column(name = "amount_deviation_percentage")
    private Double amountDeviationPercentage;

    @Column(name = "amount_behavior_risk")
    private Double amountBehaviorRisk;

    @Column(name = "failure_behavior_risk")
    private Double failureBehaviorRisk;

    @Column(name = "frequency_behavior_risk")
    private Double frequencyBehaviorRisk;

    @Column(name = "location_behavior_risk")
    private Double locationBehaviorRisk;

    @Column(name = "behavior_category")
    private String behaviorCategory;

    // =========================
    // SECURITY CHALLENGE
    // =========================

    @Column(name = "security_challenge_required", nullable = false)
    private boolean securityChallengeRequired;

    @Column(name = "security_challenge_passed", nullable = false)
    private boolean securityChallengePassed;

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getSpringRiskScore() {
        return springRiskScore;
    }

    public void setSpringRiskScore(Double springRiskScore) {
        this.springRiskScore = springRiskScore;
    }

    public Double getPythonBehavioralRisk() {
        return pythonBehavioralRisk;
    }

    public void setPythonBehavioralRisk(Double pythonBehavioralRisk) {
        this.pythonBehavioralRisk = pythonBehavioralRisk;
    }

    public Double getCombinedRisk() {
        return combinedRisk;
    }

    public void setCombinedRisk(Double combinedRisk) {
        this.combinedRisk = combinedRisk;
    }

    public Double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(Double averageAmount) {
        this.averageAmount = averageAmount;
    }

    public Double getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(Double maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public Double getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(Double minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Integer getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Integer successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public Integer getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(Integer failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public Double getFailedTransactionRatio() {
        return failedTransactionRatio;
    }

    public void setFailedTransactionRatio(Double failedTransactionRatio) {
        this.failedTransactionRatio = failedTransactionRatio;
    }

    public Double getAmountDeviationPercentage() {
        return amountDeviationPercentage;
    }

    public void setAmountDeviationPercentage(Double amountDeviationPercentage) {
        this.amountDeviationPercentage = amountDeviationPercentage;
    }

    public Double getAmountBehaviorRisk() {
        return amountBehaviorRisk;
    }

    public void setAmountBehaviorRisk(Double amountBehaviorRisk) {
        this.amountBehaviorRisk = amountBehaviorRisk;
    }

    public Double getFailureBehaviorRisk() {
        return failureBehaviorRisk;
    }

    public void setFailureBehaviorRisk(Double failureBehaviorRisk) {
        this.failureBehaviorRisk = failureBehaviorRisk;
    }

    public Double getFrequencyBehaviorRisk() {
        return frequencyBehaviorRisk;
    }

    public void setFrequencyBehaviorRisk(Double frequencyBehaviorRisk) {
        this.frequencyBehaviorRisk = frequencyBehaviorRisk;
    }

    public Double getLocationBehaviorRisk() {
        return locationBehaviorRisk;
    }

    public void setLocationBehaviorRisk(Double locationBehaviorRisk) {
        this.locationBehaviorRisk = locationBehaviorRisk;
    }

    public String getBehaviorCategory() {
        return behaviorCategory;
    }

    public void setBehaviorCategory(String behaviorCategory) {
        this.behaviorCategory = behaviorCategory;
    }

    public boolean isSecurityChallengeRequired() {
        return securityChallengeRequired;
    }

    public void setSecurityChallengeRequired(boolean securityChallengeRequired) {
        this.securityChallengeRequired = securityChallengeRequired;
    }

    public boolean isSecurityChallengePassed() {
        return securityChallengePassed;
    }

    public void setSecurityChallengePassed(boolean securityChallengePassed) {
        this.securityChallengePassed = securityChallengePassed;
    }
}
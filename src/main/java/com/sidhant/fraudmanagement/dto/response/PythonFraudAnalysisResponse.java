package com.sidhant.fraudmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PythonFraudAnalysisResponse {

    @JsonProperty("average_amount")
    private double averageAmount;

    @JsonProperty("maximum_amount")
    private double maximumAmount;

    @JsonProperty("minimum_amount")
    private double minimumAmount;

    @JsonProperty("transaction_count")
    private int transactionCount;

    @JsonProperty("successful_transactions")
    private int successfulTransactions;

    @JsonProperty("failed_transactions")
    private int failedTransactions;

    @JsonProperty("failed_transaction_ratio")
    private double failedTransactionRatio;

    @JsonProperty("amount_deviation_percentage")
    private double amountDeviationPercentage;
    @JsonProperty("amount_behavior_risk")
    private double amountBehaviorRisk;

    @JsonProperty("failure_behavior_risk")
    private double failureBehaviorRisk;

    @JsonProperty("frequency_behavior_risk")
    private double frequencyBehaviorRisk;

    @JsonProperty("location_behavior_risk")
    private double locationBehaviorRisk;

    @JsonProperty("behavioral_risk")
    private double behavioralRisk;

    @JsonProperty("behavior_category")
    private String behaviorCategory;

    public PythonFraudAnalysisResponse() {
    }

    public double getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(double averageAmount) {
        this.averageAmount = averageAmount;
    }

    public double getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(double maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public double getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(double minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public int getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(int successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public int getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(int failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public double getFailedTransactionRatio() {
        return failedTransactionRatio;
    }

    public void setFailedTransactionRatio(double failedTransactionRatio) {
        this.failedTransactionRatio = failedTransactionRatio;
    }

    public double getAmountDeviationPercentage() {
        return amountDeviationPercentage;
    }

    public void setAmountDeviationPercentage(double amountDeviationPercentage) {
        this.amountDeviationPercentage = amountDeviationPercentage;
    }
    public double getAmountBehaviorRisk() {
    return amountBehaviorRisk;
    }

    public void setAmountBehaviorRisk(double amountBehaviorRisk) {
        this.amountBehaviorRisk = amountBehaviorRisk;
    }

    public double getFailureBehaviorRisk() {
        return failureBehaviorRisk;
    }

    public void setFailureBehaviorRisk(double failureBehaviorRisk) {
        this.failureBehaviorRisk = failureBehaviorRisk;
    }

    public double getFrequencyBehaviorRisk() {
        return frequencyBehaviorRisk;
    }

    public void setFrequencyBehaviorRisk(double frequencyBehaviorRisk) {
        this.frequencyBehaviorRisk = frequencyBehaviorRisk;
    }

    public double getLocationBehaviorRisk() {
        return locationBehaviorRisk;
    }

    public void setLocationBehaviorRisk(double locationBehaviorRisk) {
        this.locationBehaviorRisk = locationBehaviorRisk;
    }

    public double getBehavioralRisk() {
        return behavioralRisk;
    }

    public void setBehavioralRisk(double behavioralRisk) {
        this.behavioralRisk = behavioralRisk;
    }

    public String getBehaviorCategory() {
        return behaviorCategory;
    }

    public void setBehaviorCategory(String behaviorCategory) {
        this.behaviorCategory = behaviorCategory;
    }
}
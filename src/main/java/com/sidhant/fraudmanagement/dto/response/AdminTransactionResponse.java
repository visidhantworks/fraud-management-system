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
            LocalDateTime createdAt
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
}
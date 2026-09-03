package com.sidhant.fraudmanagement.dto.response;

import com.sidhant.fraudmanagement.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private String transactionId;
    private Long userId;
    private BigDecimal amount;
    private Double latitude;
    private Double longitude;
    private TransactionStatus status;
    private LocalDateTime createdAt;

    public TransactionResponse(String transactionId,
                               Long userId,
                               BigDecimal amount,
                               Double latitude,
                               Double longitude,
                               TransactionStatus status,
                               LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getUserId() {
        return userId;
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

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
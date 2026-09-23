package com.sidhant.fraudmanagement.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PythonTransactionData {

    private BigDecimal amount;
    private String status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;

    public PythonTransactionData() {
    }

    public PythonTransactionData(
            BigDecimal amount,
            String status,
            Double latitude,
            Double longitude,
            LocalDateTime createdAt
    ) {
        this.amount = amount;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
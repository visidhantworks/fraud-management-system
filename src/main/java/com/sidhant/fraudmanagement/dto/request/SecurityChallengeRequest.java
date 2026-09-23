package com.sidhant.fraudmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SecurityChallengeRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String answer;

    public SecurityChallengeRequest() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
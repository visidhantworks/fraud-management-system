package com.sidhant.fraudmanagement.dto.request;

import java.util.List;

public class PythonFraudAnalysisRequest {

    private Long userId;
    private PythonTransactionData currentTransaction;
    private List<PythonTransactionData> transactionHistory;

    public PythonFraudAnalysisRequest() {
    }

    public PythonFraudAnalysisRequest(
            Long userId,
            PythonTransactionData currentTransaction,
            List<PythonTransactionData> transactionHistory
    ) {
        this.userId = userId;
        this.currentTransaction = currentTransaction;
        this.transactionHistory = transactionHistory;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PythonTransactionData getCurrentTransaction() {
        return currentTransaction;
    }

    public void setCurrentTransaction(PythonTransactionData currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    public List<PythonTransactionData> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(List<PythonTransactionData> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }
}
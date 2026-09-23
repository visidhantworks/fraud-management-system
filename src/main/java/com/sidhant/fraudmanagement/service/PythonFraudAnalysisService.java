package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.dto.request.PythonFraudAnalysisRequest;
import com.sidhant.fraudmanagement.dto.response.PythonFraudAnalysisResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PythonFraudAnalysisService {

    private final RestClient restClient;

    public PythonFraudAnalysisService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(System.getenv("PYTHON_ANALYSIS_URL"))
                .build();
    }
    public PythonFraudAnalysisResponse analyze(PythonFraudAnalysisRequest request) {

    PythonFraudAnalysisResponse response = restClient.post()
            .uri("/analyze")
            .body(request)
            .retrieve()
            .body(PythonFraudAnalysisResponse.class);

    System.out.println("===== PYTHON RESPONSE RECEIVED BY JAVA =====");
    System.out.println("Average Amount: " + response.getAverageAmount());
    System.out.println("Maximum Amount: " + response.getMaximumAmount());
    System.out.println("Minimum Amount: " + response.getMinimumAmount());
    System.out.println("Transaction Count: " + response.getTransactionCount());
    System.out.println("Successful Transactions: " + response.getSuccessfulTransactions());
    System.out.println("Failed Transactions: " + response.getFailedTransactions());
    System.out.println("Failed Transaction Ratio: " + response.getFailedTransactionRatio());
    System.out.println("Amount Deviation: " + response.getAmountDeviationPercentage());
    System.out.println(
    "Amount Behavior Risk: "
            + response.getAmountBehaviorRisk()
    );

    System.out.println(
        "Failure Behavior Risk: "
            + response.getFailureBehaviorRisk()
    );

    System.out.println(
        "Frequency Behavior Risk: "
            + response.getFrequencyBehaviorRisk()
    );

    System.out.println(
        "Location Behavior Risk: "
            + response.getLocationBehaviorRisk()
    );

    System.out.println(
        "Behavioral Risk: "
            + response.getBehavioralRisk()
    );

    System.out.println(
        "Behavior Category: "
            + response.getBehaviorCategory()
    );

        return response;
    }
}
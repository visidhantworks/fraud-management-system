package com.sidhant.fraudmanagement.controller;

import com.sidhant.fraudmanagement.dto.request.PaymentRequest;
import com.sidhant.fraudmanagement.dto.response.TransactionResponse;
import com.sidhant.fraudmanagement.service.TransactionService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasRole('USER')")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> makePayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        TransactionResponse transactionResponse = transactionService.makePayment(request);
        return ResponseEntity.ok(transactionResponse);
    }
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {

        return ResponseEntity.ok(
                transactionService.getMyTransactions()
        );
    }
}
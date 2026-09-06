package com.sidhant.fraudmanagement.repository;

import com.sidhant.fraudmanagement.entity.Transaction;
import com.sidhant.fraudmanagement.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    long countByUserIdAndCreatedAtAfter(
            Long userId,
            LocalDateTime time
    );

    long countByUserIdAndStatusAndCreatedAtAfter(
            Long userId,
            TransactionStatus status,
            LocalDateTime time
    );

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Transaction> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    List<Transaction> findAllByOrderByCreatedAtDesc();

    Optional<Transaction> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
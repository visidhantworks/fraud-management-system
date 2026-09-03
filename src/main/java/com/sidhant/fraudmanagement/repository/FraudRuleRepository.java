package com.sidhant.fraudmanagement.repository;

import com.sidhant.fraudmanagement.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    Optional<FraudRule> findByRuleCodeAndEnabledTrue(String ruleCode);
}
package com.sidhant.fraudmanagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_rule_results")
public class FraudRuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_assessment_id", nullable = false)
    private FraudAssessment fraudAssessment;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "risk_points", nullable = false)
    private Integer riskPoints;

    @Column(name = "triggered", nullable = false)
    private Boolean triggered;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public FraudAssessment getFraudAssessment() {
        return fraudAssessment;
    }

    public void setFraudAssessment(FraudAssessment fraudAssessment) {
        this.fraudAssessment = fraudAssessment;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Integer getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(Integer riskPoints) {
        this.riskPoints = riskPoints;
    }

    public Boolean getTriggered() {
        return triggered;
    }

    public void setTriggered(Boolean triggered) {
        this.triggered = triggered;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
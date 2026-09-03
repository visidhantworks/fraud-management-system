package com.sidhant.fraudmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "frm_rules")
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(precision = 15, scale = 2)
    private BigDecimal threshold;

    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;

    @Column(name = "risk_points", nullable = false)
    private Integer riskPoints;

    @Column(nullable = false)
    private Boolean enabled;

    public FraudRule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public Integer getRiskPoints() {
        return riskPoints;
    }

    public void setRiskPoints(Integer riskPoints) {
        this.riskPoints = riskPoints;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
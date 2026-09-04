package com.sidhant.fraudmanagement.service;

import com.sidhant.fraudmanagement.entity.FraudRule;
import com.sidhant.fraudmanagement.entity.Transaction;
import com.sidhant.fraudmanagement.enums.TransactionStatus;
import com.sidhant.fraudmanagement.repository.FraudRuleRepository;
import org.springframework.stereotype.Service;
import com.sidhant.fraudmanagement.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class FraudRiskService {

    private final FraudRuleRepository fraudRuleRepository;
    private final TransactionRepository transactionRepository;

    public FraudRiskService(FraudRuleRepository fraudRuleRepository,TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.fraudRuleRepository = fraudRuleRepository;
    }

    public int calculateAmountRisk(BigDecimal amount) {

        FraudRule rule = fraudRuleRepository
                .findByRuleCodeAndEnabledTrue("HIGH_TRANSACTION_AMOUNT")
                .orElse(null);

        if (rule == null || rule.getThreshold() == null) {
            return 0;
        }

        if (amount.compareTo(rule.getThreshold()) > 0) {
            return rule.getRiskPoints();
        }

        return 0;
    }
    public int calculateFrequencyRisk(Long userId) {

    FraudRule rule = fraudRuleRepository
            .findByRuleCodeAndEnabledTrue("HIGH_TRANSACTION_FREQUENCY")
            .orElse(null);

    if (rule == null ||
            rule.getThreshold() == null ||
            rule.getTimeWindowMinutes() == null) {
        return 0;
    }

    LocalDateTime windowStart = LocalDateTime.now()
            .minusMinutes(rule.getTimeWindowMinutes());

    long transactionCount =
            transactionRepository.countByUserIdAndCreatedAtAfter(
                    userId,
                    windowStart
            );

    if (transactionCount >= rule.getThreshold().longValue()) {
        return rule.getRiskPoints();
    }

    return 0;
}
    public int calculateFailedAttemptRisk(Long userId, boolean currentAttemptFailed) {
    FraudRule rule = fraudRuleRepository
            .findByRuleCodeAndEnabledTrue("HIGH_FAILED_ATTEMPTS")
            .orElse(null);

    if (rule == null
            || rule.getThreshold() == null
            || rule.getTimeWindowMinutes() == null) {
        return 0;
    }

    LocalDateTime windowStart =
            LocalDateTime.now().minusMinutes(
                    rule.getTimeWindowMinutes()
            );

    long failedAttempts =
            transactionRepository
                    .countByUserIdAndStatusAndCreatedAtAfter(
                            userId,
                            TransactionStatus.FAILED,
                            windowStart
                    );

    if (currentAttemptFailed) {
        failedAttempts++;
    }

    if (failedAttempts >= rule.getThreshold().longValue()) {
        return rule.getRiskPoints();
    }

    return 0;
}
public int calculateLocationRisk(Long userId, Double latitude, Double longitude) {

    if (latitude == null || longitude == null) {
        return 0;
    }

    FraudRule rule = fraudRuleRepository
            .findByRuleCodeAndEnabledTrue("UNUSUAL_LOCATION")
            .orElse(null);

    if (rule == null || rule.getThreshold() == null) {
        return 0;
    }

    FraudRule bucketRule = fraudRuleRepository
            .findByRuleCodeAndEnabledTrue("LOCATION_BUCKET_RADIUS")
            .orElse(null);

    if (bucketRule == null || bucketRule.getThreshold() == null) {
        return 0;
    }

    List<Transaction> previousTransactions =
            transactionRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);

    if (previousTransactions.isEmpty()) {
        return 0;
    }

    double bucketRadius = bucketRule.getThreshold().doubleValue();

    Map<Location, Integer> locationBuckets = new HashMap<>();

    for (Transaction transaction : previousTransactions) {

        if (transaction.getLatitude() == null ||
                transaction.getLongitude() == null) {
            continue;
        }

        Location currentLocation = new Location(
                transaction.getLatitude(),
                transaction.getLongitude()
        );

        Location matchingBucket = null;

        for (Location bucketLocation : locationBuckets.keySet()) {

            double distance = calculateDistance(
                    currentLocation.latitude(),
                    bucketLocation.latitude(),
                    currentLocation.longitude(),
                    bucketLocation.longitude()
            );

            if (distance <= bucketRadius) {
                matchingBucket = bucketLocation;
                break;
            }
        }

        if (matchingBucket != null) {
            locationBuckets.merge(matchingBucket, 1, Integer::sum);
        } else {
            locationBuckets.put(currentLocation, 1);
        }
    }

    Location dominantLocation = locationBuckets.entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

    if (dominantLocation == null) {
        return 0;
    }

    double distance = calculateDistance(
            latitude,
            dominantLocation.latitude(),
            longitude,
            dominantLocation.longitude()
    );

    if (distance > rule.getThreshold().doubleValue()) {
        return rule.getRiskPoints();
    }

    return 0;
}
    private double calculateDistance(Double lat1 , Double lat2 , Double lon1 , Double lon2){

    final double EARTH_RADIUS_KM = 6371.0;

    double latDifference = Math.toRadians(lat2 - lat1);
    double lonDifference = Math.toRadians(lon2 - lon1);

    double a = Math.sin(latDifference / 2) * Math.sin(latDifference / 2)+ Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2))* Math.sin(lonDifference / 2)* Math.sin(lonDifference / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_KM * c;
    }
}
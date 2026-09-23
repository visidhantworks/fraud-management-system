from statistics import mean, median
from datetime import timedelta
from app.models import FraudAnalysisRequest, FraudAnalysisResponse
from math import radians, sin, cos, sqrt, atan2

def calculate_amount_behavior_risk(amount_deviation_percentage: float) -> float:


    risk = (amount_deviation_percentage / 500) * 100

    return min(max(risk, 0), 100)
def calculate_failure_behavior_risk(failed_transaction_ratio: float) -> float:

    risk = (failed_transaction_ratio / 50) * 100

    return min(max(risk, 0), 100)
def calculate_distance_km(latitude1: float,longitude1: float,latitude2: float,longitude2: float) -> float:

    earth_radius_km = 6371.0

    lat1 = radians(latitude1)
    lon1 = radians(longitude1)
    lat2 = radians(latitude2)
    lon2 = radians(longitude2)

    delta_lat = lat2 - lat1
    delta_lon = lon2 - lon1

    a = (
        sin(delta_lat / 2) ** 2
        + cos(lat1)
        * cos(lat2)
        * sin(delta_lon / 2) ** 2
    )

    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earth_radius_km * c
def calculate_frequency_behavior_risk(history) -> tuple[int, float]:
    """
    Calculates behavioral risk based on the largest
    transaction burst within any 2-minute window.
    """

    if len(history) < 2:
        return 0, 0.0

    timestamps = sorted(
        transaction.created_at
        for transaction in history
    )

    max_transactions_in_window = 0

    for i in range(len(timestamps)):

        window_start = timestamps[i]

        window_count = 0

        for j in range(i, len(timestamps)):

            time_difference = (
                timestamps[j] - window_start
            ).total_seconds()

            if time_difference <= 120:
                window_count += 1
            else:
                break

        max_transactions_in_window = max(
            max_transactions_in_window,
            window_count
        )

    if max_transactions_in_window <= 2:
        risk = 0

    elif max_transactions_in_window <= 4:
        risk = 25

    elif max_transactions_in_window <= 6:
        risk = 50

    elif max_transactions_in_window <= 8:
        risk = 75

    else:
        risk = 100

    return max_transactions_in_window, risk
def create_location_buckets(history):


    bucket_radius_km = 100.0

    buckets = []

    for transaction in history:

        if (
            transaction.latitude is None
            or transaction.longitude is None
        ):
            continue

        transaction_location = (
            transaction.latitude,
            transaction.longitude
        )

        assigned = False

        for bucket in buckets:

            distance = calculate_distance_km(
                transaction.latitude,
                transaction.longitude,
                bucket["latitude"],
                bucket["longitude"]
            )

            if distance <= bucket_radius_km:

                bucket["count"] += 1
                assigned = True
                break

        if not assigned:

            buckets.append({
                "latitude": transaction_location[0],
                "longitude": transaction_location[1],
                "count": 1
            })

    return buckets
def calculate_location_behavior_risk(
    current_transaction,
    history
) -> tuple[float, float]:
    """
    Calculates behavioral location risk based on the
    distance from the current transaction to the nearest
    historical location bucket.
    """

    if (
        current_transaction.latitude is None
        or current_transaction.longitude is None
    ):
        return 0.0, 0.0

    buckets = create_location_buckets(history)

    if not buckets:
        return 0.0, 0.0

    nearest_distance = min(
        calculate_distance_km(
            current_transaction.latitude,
            current_transaction.longitude,
            bucket["latitude"],
            bucket["longitude"]
        )
        for bucket in buckets
    )

    if nearest_distance <= 100:
        risk = 0

    elif nearest_distance <= 300:
        risk = 25

    elif nearest_distance <= 700:
        risk = 50

    elif nearest_distance <= 1000:
        risk = 75

    else:
        risk = 100

    return nearest_distance, risk
def calculate_behavioral_risk( amount_risk: float, failure_risk: float, frequency_risk: float, location_risk: float) -> float:


    weighted_risk = (
        amount_risk * 0.25
        + failure_risk * 0.20
        + frequency_risk * 0.30
        + location_risk * 0.25
    )

    return min(max(weighted_risk, 0), 100)
def get_behavior_category(behavioral_risk: float) -> str:


    if behavioral_risk <= 30:
        return "NORMAL"

    elif behavioral_risk <= 60:
        return "SLIGHTLY_ABNORMAL"

    else:
        return "HIGHLY_ABNORMAL"
def analyze_transactions(request: FraudAnalysisRequest) -> FraudAnalysisResponse:
    print("===== PYTHON REQUEST RECEIVED =====")
    print("User ID:", request.user_id)
    print("History Size:", len(request.transaction_history))

    if request.transaction_history:
        print("First Transaction:", request.transaction_history[0])

    history = request.transaction_history
    current = request.current_transaction
    print("===== PYTHON ANALYSIS DEBUG =====")
    print("History Size:", len(history))

    amounts = [transaction.amount for transaction in history]

    print("Amounts:", amounts)
    print("Average:", mean(amounts) if amounts else 0)


    if not history:
        return FraudAnalysisResponse(
            average_amount=0.0,
            maximum_amount=0.0,
            minimum_amount=0.0,
            transaction_count=0,
            successful_transactions=0,
            failed_transactions=0,
            failed_transaction_ratio=0.0,
            amount_deviation_percentage=0.0,

            amount_behavior_risk=0.0,
            failure_behavior_risk=0.0,
            frequency_behavior_risk=0.0,
            location_behavior_risk=0.0,

            behavioral_risk=0.0,
            behavior_category="NORMAL"
        )
    amounts = [transaction.amount for transaction in history]

    average_amount = mean(amounts)
    maximum_amount = max(amounts)
    minimum_amount = min(amounts)

    successful_transactions = sum(
        1
        for transaction in history
        if transaction.status.upper() in {"SUCCESS" , "BLOCKED"}
    )

    failed_transactions = sum(
        1
        for transaction in history
        if transaction.status.upper() == "FAILED"
    )

    transaction_count = len(history)

    failed_transaction_ratio = (
        failed_transactions / transaction_count
    ) * 100

    if average_amount > 0:
        amount_deviation_percentage = (
            abs(current.amount - average_amount)
            / average_amount
        ) * 100
    else:
        amount_deviation_percentage = 0.0

    amount_behavior_risk = calculate_amount_behavior_risk(amount_deviation_percentage)

    print("Amount Behavior Risk:", amount_behavior_risk)

    failure_behavior_risk = calculate_failure_behavior_risk(failed_transaction_ratio)

    print("Failure Behavior Risk:", failure_behavior_risk)

    max_frequency_burst, frequency_behavior_risk = (calculate_frequency_behavior_risk(history))

    print(
        "Maximum Transactions In 2 Minutes:",
        max_frequency_burst
    )

    print(
        "Frequency Behavior Risk:",
        frequency_behavior_risk
    )
    nearest_location_distance, location_behavior_risk = (calculate_location_behavior_risk(current,history))

    print( "Nearest Historical Location Distance:",nearest_location_distance,"km")

    print("Location Behavior Risk:",location_behavior_risk)
    behavioral_risk = calculate_behavioral_risk(amount_behavior_risk,failure_behavior_risk,frequency_behavior_risk,location_behavior_risk)

    print("===== PYTHON BEHAVIORAL RISK =====")

    print("Behavioral Risk:",behavioral_risk)
    behavior_category = get_behavior_category(behavioral_risk)

    print(
        "Behavior Category:",
        behavior_category
    )

    print("===== PYTHON FINAL VALUES =====")
    print("Average:", average_amount)
    print("Maximum:", maximum_amount)
    print("Minimum:", minimum_amount)
    print("Transaction Count:", transaction_count)
    print("Successful:", successful_transactions)
    print("Failed:", failed_transactions)
    print("Failed Ratio:", failed_transaction_ratio)
    print("Deviation:", amount_deviation_percentage)
    return FraudAnalysisResponse(
    average_amount=round(average_amount, 2),
    maximum_amount=round(maximum_amount, 2),
    minimum_amount=round(minimum_amount, 2),
    transaction_count=transaction_count,
    successful_transactions=successful_transactions,
    failed_transactions=failed_transactions,
    failed_transaction_ratio=round(
        failed_transaction_ratio,
        2
    ),
    amount_deviation_percentage=round(
        amount_deviation_percentage,
        2
    ),

    amount_behavior_risk=round(
        amount_behavior_risk,
        2
    ),

    failure_behavior_risk=round(
        failure_behavior_risk,
        2
    ),

    frequency_behavior_risk=round(
        frequency_behavior_risk,
        2
    ),

    location_behavior_risk=round(
        location_behavior_risk,
        2
    ),

    behavioral_risk=round(
        behavioral_risk,
        2
    ),

    behavior_category=behavior_category
)
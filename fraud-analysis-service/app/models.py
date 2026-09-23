from pydantic import BaseModel, ConfigDict, Field
from typing import List, Optional
from datetime import datetime


class TransactionData(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    amount: float
    status: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    created_at: datetime = Field(alias="createdAt")


class FraudAnalysisRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    user_id: int = Field(alias="userId")
    current_transaction: TransactionData = Field(alias="currentTransaction")
    transaction_history: List[TransactionData] = Field(alias="transactionHistory")

class FraudAnalysisResponse(BaseModel):
    average_amount: float
    maximum_amount: float
    minimum_amount: float
    transaction_count: int
    successful_transactions: int
    failed_transactions: int
    failed_transaction_ratio: float
    amount_deviation_percentage: float

    amount_behavior_risk: float
    failure_behavior_risk: float
    frequency_behavior_risk: float
    location_behavior_risk: float

    behavioral_risk: float
    behavior_category: str
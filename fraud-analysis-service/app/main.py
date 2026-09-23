from fastapi import FastAPI
from app.models import FraudAnalysisRequest , FraudAnalysisResponse
from app.analyzer import analyze_transactions
app = FastAPI(
    title = 'Fraud-Analysis-Service',
    description = " Python based transaction data analysis service for the FRMS",
    version = "1.0.0"
)
@app.get("/")
def health_check():
    return {
        "service" : "Fraud-Analysis Service",
        "status" : "running"
    }
@app.post("/analyze" , response_model = FraudAnalysisResponse)
def analyze(request: FraudAnalysisRequest):
    return analyze_transactions(request)
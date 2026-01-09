from pydantic import BaseModel
from typing import Optional, Dict, Any
from datetime import datetime

# --- Order Schemas ---
class OrderCreate(BaseModel):
    amount: int
    currency: str = "INR"
    receipt: Optional[str] = None
    notes: Optional[Dict[str, Any]] = None

class OrderResponse(BaseModel):
    id: str
    merchant_id: str
    amount: int
    currency: str
    receipt: Optional[str]
    notes: Optional[Dict[str, Any]]
    status: str
    created_at: datetime
    updated_at: Optional[datetime]

# --- Payment Schemas ---
class CardDetails(BaseModel):
    number: str
    expiry_month: str
    expiry_year: str
    cvv: str
    holder_name: str

class PaymentCreate(BaseModel):
    order_id: str
    method: str
    vpa: Optional[str] = None
    card: Optional[CardDetails] = None

class PaymentResponse(BaseModel):
    id: str
    order_id: str
    amount: int
    currency: str
    method: str
    status: str
    vpa: Optional[str] = None
    card_network: Optional[str] = None
    card_last4: Optional[str] = None
    error_code: Optional[str] = None
    error_description: Optional[str] = None
    created_at: datetime
    updated_at: Optional[datetime]
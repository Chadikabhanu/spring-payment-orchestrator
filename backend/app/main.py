from typing import List
from fastapi import FastAPI, Depends, HTTPException, Header, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime
import uuid
import time
import os
import random

# Import our modules
from .database import engine, Base, get_db
from .models import Merchant, Order, Payment
from .utils import generate_id, validate_luhn, validate_vpa, get_card_network, validate_expiry
from .schemas import OrderCreate, OrderResponse, PaymentCreate, PaymentResponse

# Create Database Tables
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Payment Gateway API")

# --- CORS MIDDLEWARE (Critical for Frontend connection) ---
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
# ----------------------------------------------------------

# --- Startup Event ---
@app.on_event("startup")
def startup_event():
    db = next(get_db())
    try:
        test_email = "test@example.com"
        existing = db.query(Merchant).filter(Merchant.email == test_email).first()
        if not existing:
            test_merchant = Merchant(
                id=uuid.UUID("550e8400-e29b-41d4-a716-446655440000"),
                name="Test Merchant",
                email=test_email,
                api_key="key_test_abc123",
                api_secret="secret_test_xyz789"
            )
            db.add(test_merchant)
            db.commit()
            print("✅ Test merchant seeded successfully.")
    except Exception as e:
        print(f"❌ Error seeding merchant: {e}")
    finally:
        db.close()

# --- Authentication Dependency ---
def get_current_merchant(
    x_api_key: str = Header(..., alias="X-Api-Key"),
    x_api_secret: str = Header(..., alias="X-Api-Secret"),
    db: Session = Depends(get_db)
):
    merchant = db.query(Merchant).filter(
        Merchant.api_key == x_api_key,
        Merchant.api_secret == x_api_secret
    ).first()
    
    if not merchant:
        raise HTTPException(
            status_code=401,
            detail={"error": {"code": "AUTHENTICATION_ERROR", "description": "Invalid API credentials"}}
        )
    return merchant

# --- Health Check ---
@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "database": "connected",
        "timestamp": datetime.utcnow().isoformat() + "Z"
    }

# --- Test Endpoint ---
@app.get("/api/v1/test/merchant")
def get_test_merchant(db: Session = Depends(get_db)):
    merchant = db.query(Merchant).filter(Merchant.email == "test@example.com").first()
    if not merchant:
        raise HTTPException(status_code=404, detail="Test merchant not found")
    return {
        "id": str(merchant.id),
        "email": merchant.email,
        "api_key": merchant.api_key,
        "seeded": True
    }

# --- Order Endpoints ---

@app.post("/api/v1/orders", response_model=OrderResponse, status_code=201)
def create_order(
    order_data: OrderCreate, 
    merchant: Merchant = Depends(get_current_merchant), 
    db: Session = Depends(get_db)
):
    if order_data.amount < 100:
        raise HTTPException(
            status_code=400, 
            detail={"error": {"code": "BAD_REQUEST_ERROR", "description": "amount must be at least 100"}}
        )

    new_order_id = generate_id("order")
    
    new_order = Order(
        id=new_order_id,
        merchant_id=merchant.id,
        amount=order_data.amount,
        currency=order_data.currency,
        receipt=order_data.receipt,
        notes=order_data.notes,
        status="created"
    )
    
    db.add(new_order)
    db.commit()
    db.refresh(new_order)
    
    response = OrderResponse(
        id=new_order.id,
        merchant_id=str(new_order.merchant_id),
        amount=new_order.amount,
        currency=new_order.currency,
        receipt=new_order.receipt,
        notes=new_order.notes if new_order.notes else {},
        status=new_order.status,
        created_at=new_order.created_at,
        updated_at=new_order.updated_at
    )
    return response

@app.get("/api/v1/orders/{order_id}", response_model=OrderResponse)
def get_order(
    order_id: str,
    merchant: Merchant = Depends(get_current_merchant),
    db: Session = Depends(get_db)
):
    order = db.query(Order).filter(Order.id == order_id, Order.merchant_id == merchant.id).first()
    
    if not order:
        raise HTTPException(
            status_code=404,
            detail={"error": {"code": "NOT_FOUND_ERROR", "description": "Order not found"}}
        )
        
    return OrderResponse(
        id=order.id,
        merchant_id=str(order.merchant_id),
        amount=order.amount,
        currency=order.currency,
        receipt=order.receipt,
        notes=order.notes if order.notes else {},
        status=order.status,
        created_at=order.created_at,
        updated_at=order.updated_at
    )

# --- Payment Endpoints ---

@app.post("/api/v1/payments", response_model=PaymentResponse, status_code=201)
def create_payment(
    payment_data: PaymentCreate,
    merchant: Merchant = Depends(get_current_merchant),
    db: Session = Depends(get_db)
):
    # 1. Verify Order exists and belongs to merchant
    order = db.query(Order).filter(
        Order.id == payment_data.order_id, 
        Order.merchant_id == merchant.id
    ).first()
    
    if not order:
        raise HTTPException(
            status_code=404, 
            detail={"error": {"code": "NOT_FOUND_ERROR", "description": "Order not found"}}
        )

    # 2. Method Specific Validation
    card_network = None
    card_last4 = None
    
    if payment_data.method == "upi":
        if not validate_vpa(payment_data.vpa):
             raise HTTPException(
                status_code=400, 
                detail={"error": {"code": "INVALID_VPA", "description": "Invalid VPA format"}}
            )
            
    elif payment_data.method == "card":
        card = payment_data.card
        if not card:
             raise HTTPException(
                status_code=400, 
                detail={"error": {"code": "BAD_REQUEST_ERROR", "description": "Card details required"}}
            )
            
        if not validate_luhn(card.number):
             raise HTTPException(
                status_code=400, 
                detail={"error": {"code": "INVALID_CARD", "description": "Invalid card number"}}
            )
            
        if not validate_expiry(card.expiry_month, card.expiry_year):
             raise HTTPException(
                status_code=400, 
                detail={"error": {"code": "EXPIRED_CARD", "description": "Card has expired"}}
            )
            
        # Store limited card info
        card_network = get_card_network(card.number)
        card_last4 = card.number[-4:]

    # 3. Create Payment Record (Status: Processing)
    payment_id = generate_id("pay")
    
    new_payment = Payment(
        id=payment_id,
        order_id=order.id,
        merchant_id=merchant.id,
        amount=order.amount,
        currency=order.currency,
        method=payment_data.method,
        status="processing", # Skip 'created'
        vpa=payment_data.vpa,
        card_network=card_network,
        card_last4=card_last4
    )
    
    db.add(new_payment)
    db.commit()
    
    # 4. SIMULATION LOGIC
    test_mode = os.getenv("TEST_MODE", "false").lower() == "true"
    
    if test_mode:
        success = os.getenv("TEST_PAYMENT_SUCCESS", "true").lower() == "true"
        delay = int(os.getenv("TEST_PROCESSING_DELAY", "1000")) / 1000.0
    else:
        delay = random.uniform(5, 10)
        if payment_data.method == "upi":
            success = random.random() < 0.90
        else:
            success = random.random() < 0.95
            
    time.sleep(delay)
    
    if success:
        new_payment.status = "success"
    else:
        new_payment.status = "failed"
        new_payment.error_code = "PAYMENT_FAILED"
        new_payment.error_description = "Bank declined transaction"
        
    db.commit()
    db.refresh(new_payment)
    
    return PaymentResponse(
        id=new_payment.id,
        order_id=new_payment.order_id,
        amount=new_payment.amount,
        currency=new_payment.currency,
        method=new_payment.method,
        status=new_payment.status,
        vpa=new_payment.vpa,
        card_network=new_payment.card_network,
        card_last4=new_payment.card_last4,
        error_code=new_payment.error_code,
        error_description=new_payment.error_description,
        created_at=new_payment.created_at,
        updated_at=new_payment.updated_at
    )

@app.get("/api/v1/payments/{payment_id}", response_model=PaymentResponse)
def get_payment(
    payment_id: str,
    merchant: Merchant = Depends(get_current_merchant),
    db: Session = Depends(get_db)
):
    payment = db.query(Payment).filter(
        Payment.id == payment_id,
        Payment.merchant_id == merchant.id
    ).first()
    
    if not payment:
        raise HTTPException(
            status_code=404,
            detail={"error": {"code": "NOT_FOUND_ERROR", "description": "Payment not found"}}
        )
        
    return PaymentResponse(
        id=payment.id,
        order_id=payment.order_id,
        amount=payment.amount,
        currency=payment.currency,
        method=payment.method,
        status=payment.status,
        vpa=payment.vpa,
        card_network=payment.card_network,
        card_last4=payment.card_last4,
        error_code=payment.error_code,
        error_description=payment.error_description,
        created_at=payment.created_at,
        updated_at=payment.updated_at
    )

# --- LIST PAYMENTS (For Dashboard) ---
@app.get("/api/v1/payments", response_model=List[PaymentResponse])
def list_payments(
    merchant: Merchant = Depends(get_current_merchant),
    db: Session = Depends(get_db)
):
    payments = db.query(Payment).filter(
        Payment.merchant_id == merchant.id
    ).order_by(Payment.created_at.desc()).all()
    
    return [
        PaymentResponse(
            id=p.id,
            order_id=p.order_id,
            amount=p.amount,
            currency=p.currency,
            method=p.method,
            status=p.status,
            vpa=p.vpa,
            card_network=p.card_network,
            card_last4=p.card_last4,
            error_code=p.error_code,
            error_description=p.error_description,
            created_at=p.created_at,
            updated_at=p.updated_at
        ) for p in payments
    ]
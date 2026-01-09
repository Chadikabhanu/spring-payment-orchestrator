import uuid
from datetime import datetime
from sqlalchemy import Column, String, Integer, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.dialects.postgresql import UUID, JSON  # <--- Added JSON here
from sqlalchemy.orm import relationship
from .database import Base

class Merchant(Base):
    __tablename__ = "merchants"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=False)
    api_key = Column(String(64), unique=True, nullable=False)
    api_secret = Column(String(64), nullable=False)
    webhook_url = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class Order(Base):
    __tablename__ = "orders"
    
    id = Column(String(64), primary_key=True)
    merchant_id = Column(UUID(as_uuid=True), ForeignKey("merchants.id"), nullable=False)
    amount = Column(Integer, nullable=False)
    currency = Column(String(3), default="INR")
    receipt = Column(String(255), nullable=True)
    
    # FIX: Changed from Text to JSON
    notes = Column(JSON, nullable=True)
    
    status = Column(String(20), default="created")
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class Payment(Base):
    __tablename__ = "payments"
    
    id = Column(String(64), primary_key=True)
    order_id = Column(String(64), ForeignKey("orders.id"), nullable=False)
    merchant_id = Column(UUID(as_uuid=True), ForeignKey("merchants.id"), nullable=False)
    amount = Column(Integer, nullable=False)
    currency = Column(String(3), default="INR")
    method = Column(String(20), nullable=False)
    status = Column(String(20), default="processing")
    vpa = Column(String(255), nullable=True)
    card_network = Column(String(20), nullable=True)
    card_last4 = Column(String(4), nullable=True)
    error_code = Column(String(50), nullable=True)
    error_description = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
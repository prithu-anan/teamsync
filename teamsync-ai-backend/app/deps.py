# app/deps.py
from app.db import (
    AuthSessionLocal,
    ProjectSessionLocal, 
    TaskSessionLocal, 
    UserSessionLocal, 
    MessageSessionLocal,
    DATABASE_MAPPING
)
from sqlalchemy.orm import Session
from typing import Generator

# Dependency to get the appropriate DB session based on model type
def get_db_for_model(model_type: str) -> Generator[Session, None, None]:
    """Get database session for specific model type"""
    if model_type == "auth":
        db = AuthSessionLocal()
    elif model_type == "project":
        db = ProjectSessionLocal()
    elif model_type == "task":
        db = TaskSessionLocal()
    elif model_type == "user":
        db = UserSessionLocal()
    elif model_type in ["message", "channel"]:
        db = MessageSessionLocal()
    else:
        raise ValueError(f"Unknown model type: {model_type}")
    
    try:
        yield db
    finally:
        db.close()

# Legacy function for backward compatibility (defaults to project DB)
def get_db():
    db = ProjectSessionLocal()
    try:
        yield db
    finally:
        db.close()

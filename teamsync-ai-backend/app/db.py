from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

# Database URLs for different microservices
AUTH_DATABASE_URL = os.getenv("AUTH_DATABASE_URL")
PROJECT_DATABASE_URL = os.getenv("PROJECT_DATABASE_URL")
TASK_DATABASE_URL = os.getenv("TASK_DATABASE_URL")
USER_DATABASE_URL = os.getenv("USER_DATABASE_URL")
MESSAGE_DATABASE_URL = os.getenv("MESSAGE_DATABASE_URL")

# Create engines for each database
auth_engine = create_engine(AUTH_DATABASE_URL)
project_engine = create_engine(PROJECT_DATABASE_URL)
task_engine = create_engine(TASK_DATABASE_URL)
user_engine = create_engine(USER_DATABASE_URL)
message_engine = create_engine(MESSAGE_DATABASE_URL)

# Create session makers for each database
AuthSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=auth_engine)
ProjectSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=project_engine)
TaskSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=task_engine)
UserSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=user_engine)
MessageSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=message_engine)

# Base class for all models
Base = declarative_base()

# Database mapping for different models
DATABASE_MAPPING = {
    "auth": auth_engine,
    "project": project_engine,
    "task": task_engine,
    "user": user_engine,
    "message": message_engine,
    "channel": message_engine
}

from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from enum import Enum

class TaskStatus(str, Enum):
    backlog = "backlog"
    todo = "todo"
    in_progress = "in_progress"
    in_review = "in_review"
    blocked = "blocked"
    completed = "completed"

class TaskPriority(str, Enum):
    low = "low"
    medium = "medium"
    high = "high"
    urgent = "urgent"

class ChannelType(str, Enum):
    direct = "direct"
    group = "group"

class User(BaseModel):
    id: int
    name: str
    email: str
    designation: Optional[str] = None
    profile_picture_url: Optional[str] = None

class Project(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    created_by: int
    created_at: datetime

class Task(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    status: TaskStatus
    priority: Optional[TaskPriority] = None
    time_estimate: Optional[str] = None
    ai_time_estimate: Optional[str] = None
    ai_priority: Optional[TaskPriority] = None
    project_id: int
    assigned_to: Optional[int] = None
    parent_task_id: Optional[int] = None

class Channel(BaseModel):
    id: int
    name: str
    type: ChannelType
    project_id: Optional[int] = None
    members: Optional[List[int]] = None

class Message(BaseModel):
    id: int
    sender_id: int
    channel_id: Optional[int] = None
    recipient_id: Optional[int] = None
    content: str
    timestamp: datetime
    thread_parent_id: Optional[int] = None
from fastapi import APIRouter, Depends, HTTPException, Header
from pydantic import BaseModel
from typing import Optional, List
import re
from collections import Counter
from app.clients import ProjectClient, TaskClient, UserClient
from app.deps import get_project_client, get_task_client, get_user_client
from app.llm.factory import get_llm_provider

router = APIRouter()
llm = get_llm_provider("gemini")

class EstimateDeadlineRequest(BaseModel):
    title: str
    description: str
    project_id: int
    parent_task_id: Optional[int] = None

def get_example_tasks(tasks: List[dict], parent_task_id: Optional[int] = None, limit: int = 3) -> List[dict]:
    """Filter tasks to get examples for prompting"""
    if parent_task_id:
        # Find parent task and siblings
        parent_task = next((t for t in tasks if t["id"] == parent_task_id), None)
        if parent_task:
            examples = [parent_task]
            siblings = [t for t in tasks if t.get("parentTaskId") == parent_task_id][:limit-1]
            examples.extend(siblings)
            return examples[:limit]
    
    # Return tasks with priority and time estimate
    filtered_tasks = [t for t in tasks if t.get("priority") and t.get("timeEstimate")]
    return filtered_tasks[:limit]

def construct_prompt(project: dict, example_tasks: List[dict], new_task: EstimateDeadlineRequest) -> str:
    prompt = "You are an AI assistant for an IT farm. Your task is to estimate the priority and time required for a new task based on its title, description, and related tasks in the project.\n\n"
    prompt += "Here is the project information:\n\n"
    prompt += f"Project Title: {project['title']}\n"
    prompt += f"Project Description: {project.get('description', 'No description available')}\n\n"
    
    if example_tasks:
        prompt += "Here are some example tasks in the project:\n\n"
        for i, task in enumerate(example_tasks, 1):
            prompt += f"Task {i}:\n"
            prompt += f"Title: {task['title']}\n"
            prompt += f"Description: {task.get('description', 'No description')}\n"
            prompt += f"Priority: {task.get('priority', 'Not set')}\n"
            prompt += f"Time Estimate: {task.get('timeEstimate', 'Not set')}\n\n"
    
    prompt += "Now, here is the new task:\n\n"
    prompt += f"Title: {new_task.title}\n"
    prompt += f"Description: {new_task.description}\n\n"
    prompt += "Based on the above information, please estimate the priority (choose from 'low', 'medium', 'high', 'urgent') and the estimated time (in hours) for this new task. Also, provide a brief explanation for your reasoning.\n\n"
    prompt += "Please format your response as follows:\n\n"
    prompt += "Priority: [priority]\n"
    prompt += "Estimated Time: [time] hours\n"
    prompt += "Comment: [explanation]\n"
    return prompt

def parse_response(response: str) -> Optional[dict]:
    priority_match = re.search(r"Priority:\s*(\w+)", response, re.IGNORECASE)
    time_match = re.search(r"Estimated Time:\s*([\d.]+)\s*hours", response, re.IGNORECASE)
    comment_match = re.search(r"Comment:\s*(.+)", response, re.DOTALL)
    
    if priority_match and time_match and comment_match:
        priority = priority_match.group(1).lower()
        allowed_priorities = {"low", "medium", "high", "urgent"}
        if priority not in allowed_priorities:
            return None
        try:
            time = float(time_match.group(1))
            if time <= 0:
                return None
        except ValueError:
            return None
        comment = comment_match.group(1).strip()
        return {"priority": priority, "estimated_time": time, "comment": comment}
    return None

def format_time(hours: float) -> str:
    if hours >= 24:
        days = hours / 24
        return f"{days:.1f} days"
    return f"{hours:.1f} hours"

def aggregate_responses(parsed_responses: List[dict]) -> dict:
    if not parsed_responses:
        return None
    priorities = [resp["priority"] for resp in parsed_responses]
    times = [resp["estimated_time"] for resp in parsed_responses]
    priority_counter = Counter(priorities)
    most_common_priority = priority_counter.most_common(1)[0][0]
    average_time = sum(times) / len(times)
    formatted_time = format_time(average_time)
    comment = parsed_responses[0]["comment"]
    return {
        "priority": most_common_priority,
        "estimated_time": formatted_time,
        "comment": comment
    }

@router.post("/tasks/estimate-deadline")
async def estimate_deadline(
    request: EstimateDeadlineRequest,
    authorization: str = Header(..., description="JWT token in format: Bearer <token>"),
    project_client: ProjectClient = Depends(get_project_client),
    task_client: TaskClient = Depends(get_task_client),
    user_client: UserClient = Depends(get_user_client)
):
    # Extract JWT token
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header format")
    
    jwt_token = authorization[7:]
    
    # Fetch project
    project = await project_client.get_project_by_id(request.project_id, jwt_token)
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    # Fetch project tasks for examples
    tasks = await task_client.get_tasks_by_project(request.project_id, jwt_token)
    example_tasks = get_example_tasks(tasks, request.parent_task_id)

    # Construct LLM prompt
    prompt = construct_prompt(project, example_tasks, request)

    print(f"prompt: {prompt}")

    # Get LLM responses
    try:
        responses = llm.generate(prompt)
        print(f"llm said: {responses}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM request failed: {str(e)}")

    # Parse responses
    parsed_responses = [parse_response(resp) for resp in responses if parse_response(resp)]
    print(f"parsing: {parsed_responses}")
    if not parsed_responses:
        raise HTTPException(status_code=500, detail="Failed to parse valid responses from LLM")

    # Aggregate results
    result = aggregate_responses(parsed_responses)
    print(f"aggregate: {result}")
    if not result:
        raise HTTPException(status_code=500, detail="Failed to aggregate LLM responses")

    return result
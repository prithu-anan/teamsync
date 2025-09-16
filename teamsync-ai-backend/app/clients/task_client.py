import httpx
from typing import Optional, Dict, Any, List
import logging

logger = logging.getLogger(__name__)

class TaskClient:
    def __init__(self, base_url: str = "http://task-management-service:8089"):
        self.base_url = base_url
        
    async def get_task_by_id(self, task_id: int, jwt_token: str) -> Optional[Dict[str, Any]]:
        """Get task by ID"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/api/tasks/{task_id}",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching task {task_id}: {str(e)}")
            return None
    
    async def get_tasks_by_project(self, project_id: int, jwt_token: str) -> List[Dict[str, Any]]:
        """Get tasks by project ID"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/api/tasks/project/{project_id}",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return []
        except Exception as e:
            logger.error(f"Error fetching tasks for project {project_id}: {str(e)}")
            return []
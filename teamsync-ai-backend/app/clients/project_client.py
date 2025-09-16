import httpx
from typing import Optional, Dict, Any, List
import logging

logger = logging.getLogger(__name__)

class ProjectClient:
    def __init__(self, base_url: str = "http://project-management-service:8083"):
        self.base_url = base_url
        
    async def get_project_by_id(self, project_id: int, jwt_token: str) -> Optional[Dict[str, Any]]:
        """Get project by ID"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/api/projects/{project_id}",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching project {project_id}: {str(e)}")
            return None
    
    async def project_exists(self, project_id: int, jwt_token: str) -> bool:
        """Check if project exists"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/api/projects/{project_id}/exists",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return False
        except Exception as e:
            logger.error(f"Error checking project existence {project_id}: {str(e)}")
            return False
import httpx
from typing import Optional, Dict, Any
import logging

logger = logging.getLogger(__name__)

class UserClient:
    def __init__(self, base_url: str = "http://user-management-service:8082"):
        self.base_url = base_url
        
    async def get_user_by_id(self, user_id: int) -> Optional[Dict[str, Any]]:
        """Get user by ID"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{self.base_url}/api/users/{user_id}")
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching user {user_id}: {str(e)}")
            return None
    
    async def get_user_by_email(self, email: str) -> Optional[Dict[str, Any]]:
        """Get user by email"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{self.base_url}/api/users/email/{email}")
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching user by email {email}: {str(e)}")
            return None
    
    async def user_exists(self, user_id: int) -> bool:
        """Check if user exists"""
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{self.base_url}/api/users/exists/{user_id}")
                if response.status_code == 200:
                    return response.json()["data"]
                return False
        except Exception as e:
            logger.error(f"Error checking user existence {user_id}: {str(e)}")
            return False
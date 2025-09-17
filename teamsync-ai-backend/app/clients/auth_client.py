import httpx
from typing import Optional, Dict, Any
import logging

logger = logging.getLogger(__name__)

class AuthClient:
    def __init__(self, base_url: str = "http://auth-service:8081"):
        self.base_url = base_url
        
    async def get_current_user(self, jwt_token: str) -> Optional[Dict[str, Any]]:
        """Get current user information from auth service"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/auth/me",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching current user: {str(e)}")
            return None
    
    async def validate_token(self, jwt_token: str) -> bool:
        """Validate JWT token with auth service"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/auth/validate",
                    headers=headers
                )
                return response.status_code == 200
        except Exception as e:
            logger.error(f"Error validating token: {str(e)}")
            return False
import httpx
from typing import Optional, Dict, Any, List
import logging

logger = logging.getLogger(__name__)

class MessageClient:
    # def __init__(self, base_url: str = "http://message-management-service:8091"):
    def __init__(self, base_url: str = "http://api-gateway:8080"):
        self.base_url = base_url
        
    async def get_channel_messages(self, channel_id: int, jwt_token: str) -> List[Dict[str, Any]]:
        """Get messages from a channel"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/channels/{channel_id}/messages",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return []
        except Exception as e:
            logger.error(f"Error fetching messages for channel {channel_id}: {str(e)}")
            return []
    
    async def get_channel_by_id(self, channel_id: int, jwt_token: str) -> Optional[Dict[str, Any]]:
        """Get channel by ID"""
        try:
            headers = {"Authorization": f"Bearer {jwt_token}"}
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{self.base_url}/channels/{channel_id}",
                    headers=headers
                )
                if response.status_code == 200:
                    return response.json()["data"]
                return None
        except Exception as e:
            logger.error(f"Error fetching channel {channel_id}: {str(e)}")
            return None
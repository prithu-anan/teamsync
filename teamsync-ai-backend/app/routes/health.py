from fastapi import APIRouter, Depends, HTTPException
from app.clients import UserClient, ProjectClient, TaskClient, MessageClient
from app.deps import get_user_client, get_project_client, get_task_client, get_message_client
import httpx

router = APIRouter()

@router.get("/health")
async def health_check(
    user_client: UserClient = Depends(get_user_client),
    project_client: ProjectClient = Depends(get_project_client),
    task_client: TaskClient = Depends(get_task_client),
    message_client: MessageClient = Depends(get_message_client)
):
    try:
        # Test connections to all microservices
        services_status = {}
        
        # Test User Service
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{user_client.base_url}/actuator/health", timeout=5.0)
                services_status["user_service"] = "healthy" if response.status_code == 200 else "unhealthy"
        except:
            services_status["user_service"] = "unreachable"
        
        # Test Project Service
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{project_client.base_url}/actuator/health", timeout=5.0)
                services_status["project_service"] = "healthy" if response.status_code == 200 else "unhealthy"
        except:
            services_status["project_service"] = "unreachable"
        
        # Test Task Service
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{task_client.base_url}/actuator/health", timeout=5.0)
                services_status["task_service"] = "healthy" if response.status_code == 200 else "unhealthy"
        except:
            services_status["task_service"] = "unreachable"
        
        # Test Message Service
        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(f"{message_client.base_url}/actuator/health", timeout=5.0)
                services_status["message_service"] = "healthy" if response.status_code == 200 else "unhealthy"
        except:
            services_status["message_service"] = "unreachable"
        
        overall_status = "healthy" if all(status == "healthy" for status in services_status.values()) else "degraded"
        
        return {
            "status": overall_status,
            "message": "AI Backend health check",
            "services": services_status
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Health check failed: {str(e)}")
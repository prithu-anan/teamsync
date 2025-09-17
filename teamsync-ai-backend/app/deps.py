from app.clients import UserClient, ProjectClient, TaskClient, MessageClient, AuthClient

def get_user_client() -> UserClient:
    return UserClient()

def get_project_client() -> ProjectClient:
    return ProjectClient()

def get_task_client() -> TaskClient:
    return TaskClient()

def get_message_client() -> MessageClient:
    return MessageClient()

def get_auth_client() -> AuthClient:
    return AuthClient()
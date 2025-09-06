# Stop all Docker Compose services
docker compose down

# Or if you have individual docker-compose files:
cd auth-service && docker compose down && cd ..
cd user-management-service && docker compose down && cd ..
cd project-management-service && docker compose down && cd ..
cd task-management-service && docker compose down && cd ..
cd message-management-service && docker compose down && cd ..
cd feed-management-service && docker compose down && cd ..
cd api-gateway && docker compose down && cd ..
cd teamsync-ai-backend && docker compose down && cd ..
cd teamsync-social-flow && docker compose down && cd ..
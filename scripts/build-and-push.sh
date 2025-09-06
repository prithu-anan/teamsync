#!/bin/bash
set -e

# Configuration
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"prithuanan"}
IMAGE_TAG=${1:-"latest"}

# Available services
declare -A SERVICES=(
    ["auth"]="auth-service"
    ["user"]="user-management-service"
    ["project"]="project-management-service"
    ["task"]="task-management-service"
    ["message"]="message-management-service"
    ["feed"]="feed-management-service"
    ["api-gateway"]="api-gateway"
    ["ai-backend"]="teamsync-ai-backend"
    ["frontend"]="teamsync-social-flow"
)

# Function to show usage
show_usage() {
    echo "Usage: $0 [IMAGE_TAG] [SERVICE_NAMES...]"
    echo ""
    echo "Arguments:"
    echo "  IMAGE_TAG      Docker image tag (default: latest)"
    echo "  SERVICE_NAMES  Space-separated list of services to build"
    echo ""
    echo "Available services:"
    for service in "${!SERVICES[@]}"; do
        echo "  - $service"
    done
    echo ""
    echo "Examples:"
    echo "  $0                                    # Build all services with 'latest' tag"
    echo "  $0 v1.0.0                            # Build all services with 'v1.0.0' tag"
    echo "  $0 latest auth user                  # Build only auth and user services"
    echo "  $0 v1.0.0 frontend api-gateway       # Build only frontend and api-gateway with 'v1.0.0' tag"
}

# Parse arguments
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
    show_usage
    exit 0
fi

# Get services to build
if [ $# -eq 1 ]; then
    # Only IMAGE_TAG provided, build all services
    SERVICES_TO_BUILD=("${!SERVICES[@]}")
elif [ $# -gt 1 ]; then
    # IMAGE_TAG and specific services provided
    IMAGE_TAG=$1
    shift
    SERVICES_TO_BUILD=("$@")
else
    # No arguments, build all services with default tag
    SERVICES_TO_BUILD=("${!SERVICES[@]}")
fi

# Validate service names
for service in "${SERVICES_TO_BUILD[@]}"; do
    if [[ ! ${SERVICES[$service]+_} ]]; then
        echo "❌ Error: Unknown service '$service'"
        echo ""
        show_usage
        exit 1
    fi
done

echo "🔨 Building and pushing Docker images to Docker Hub"
echo "=================================================="
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"
echo "Image Tag: $IMAGE_TAG"
echo "Services to build: ${SERVICES_TO_BUILD[*]}"
echo ""

# Login to Docker Hub
echo "🔐 Logging in to Docker Hub..."
docker login

# Function to build and push a service
build_and_push_service() {
    local service_key=$1
    local service_dir=${SERVICES[$service_key]}
    local image_name="teamsync-${service_key}"
    
    echo "🏗️  Building $service_key service..."
    cd "$service_dir"
    
    # Build based on service type
    if [[ "$service_key" == "ai-backend" ]]; then
        # AI Backend (Python) - no Maven build needed
        docker build -t $DOCKER_HUB_USERNAME/$image_name:$IMAGE_TAG .
        docker build -t $DOCKER_HUB_USERNAME/$image_name:latest .
    elif [[ "$service_key" == "frontend" ]]; then
        # Frontend (Node.js) - npm build
        npm ci
        npm run build
        docker build -t $DOCKER_HUB_USERNAME/$image_name:$IMAGE_TAG .
        docker build -t $DOCKER_HUB_USERNAME/$image_name:latest .
    else
        # Java services - Maven build
        ./mvnw clean package -DskipTests
        docker build -t $DOCKER_HUB_USERNAME/$image_name:$IMAGE_TAG .
        docker build -t $DOCKER_HUB_USERNAME/$image_name:latest .
    fi
    
    # Push images
    docker push $DOCKER_HUB_USERNAME/$image_name:$IMAGE_TAG
    docker push $DOCKER_HUB_USERNAME/$image_name:latest
    
    cd ..
    echo "✅ $service_key service built and pushed successfully!"
    echo ""
}

# Build and push selected services
for service in "${SERVICES_TO_BUILD[@]}"; do
    build_and_push_service "$service"
done

echo "🎉 All selected images built and pushed successfully!"
echo "=================================================="
echo "Images pushed to: $DOCKER_HUB_USERNAME/teamsync-*:$IMAGE_TAG"
echo "Images pushed to: $DOCKER_HUB_USERNAME/teamsync-*:latest"
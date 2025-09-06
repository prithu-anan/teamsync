#!/bin/bash
set -e

# Configuration
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"prithuanan"}
IMAGE_TAG=${1:-"latest"}

# Function to show usage
show_usage() {
    echo "Usage: $0 [IMAGE_TAG] [OPTIONS]"
    echo ""
    echo "Arguments:"
    echo "  IMAGE_TAG      Docker image tag (default: latest)"
    echo ""
    echo "Options:"
    echo "  --no-pull      Skip pulling images from Docker Hub"
    echo "  --pull         Force pull images from Docker Hub (default behavior)"
    echo "  -h, --help     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Use 'latest' tag and pull images"
    echo "  $0 v1.0.0                            # Use 'v1.0.0' tag and pull images"
    echo "  $0 latest --no-pull                  # Use 'latest' tag but skip pulling"
    echo "  $0 v1.0.0 --no-pull                  # Use 'v1.0.0' tag but skip pulling"
}

# Parse arguments
PULL_IMAGES=true
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_usage
            exit 0
            ;;
        --no-pull)
            PULL_IMAGES=false
            shift
            ;;
        --pull)
            PULL_IMAGES=true
            shift
            ;;
        -*)
            echo "❌ Error: Unknown option '$1'"
            echo ""
            show_usage
            exit 1
            ;;
        *)
            if [[ -z "$IMAGE_TAG" || "$IMAGE_TAG" == "latest" ]]; then
                IMAGE_TAG=$1
            else
                echo "❌ Error: Multiple IMAGE_TAG arguments provided"
                echo ""
                show_usage
                exit 1
            fi
            shift
            ;;
    esac
done

echo "🚀 Starting TeamSync with Local Kubernetes (Minikube)"
echo "====================================================="
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"
echo "Image Tag: $IMAGE_TAG"
echo "Pull Images: $PULL_IMAGES"
echo ""

# Start Minikube
echo "🏗️  Starting Minikube..."
minikube start --memory=4096 --cpus=2

# Enable ingress
echo "🌐 Enabling ingress..."
minikube addons enable ingress

# Set Docker environment
echo "🐳 Setting Docker environment..."
eval $(minikube docker-env)

# Pull images from Docker Hub (if enabled)
if [[ "$PULL_IMAGES" == "true" ]]; then
    echo "📥 Pulling images from Docker Hub..."
    docker pull $DOCKER_HUB_USERNAME/teamsync-auth:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-user:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-project:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-task:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-message:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-feed:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-api-gateway:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-ai-backend:$IMAGE_TAG
    docker pull $DOCKER_HUB_USERNAME/teamsync-frontend:$IMAGE_TAG
    echo "✅ Images pulled successfully!"
else
    echo "⏭️  Skipping image pull (using local images)"
fi

# Apply Kubernetes manifests
echo "📦 Deploying to Kubernetes..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/infrastructure.yaml
kubectl apply -f k8s/database.yaml
kubectl apply -f k8s/services.yaml
kubectl apply -f k8s/api-gateway.yaml
kubectl apply -f k8s/ai-backend.yaml
kubectl apply -f k8s/frontend.yaml

# Wait for deployment
echo "⏳ Waiting for deployment to complete..."
kubectl wait --for=condition=available --timeout=300s deployment --all -n teamsync

# Get service URLs
echo ""
echo "✅ TeamSync is now running on Kubernetes!"
echo "========================================"
echo "Frontend: http://$(minikube ip):30000"
echo "API Gateway: http://$(minikube ip):30001"
echo "AI Backend: http://$(minikube ip):30002"
echo ""
echo "To view logs: kubectl logs -f deployment/auth-service -n teamsync"
echo "To stop: minikube stop"
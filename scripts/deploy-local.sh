#!/bin/bash
set -e

# Load environment variables
if [ -f .env.local ]; then
    export $(cat .env.local | grep -v '#' | awk '/=/ {print $1}')
fi

# Configuration
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"prithuanan"}
IMAGE_TAG=${1:-"latest"}

echo "🚀 Complete Local Deployment Workflow"
echo "===================================="
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"
echo "Image Tag: $IMAGE_TAG"
echo ""

# Step 1: Build and push images
echo "Step 1: Building and pushing images..."
./scripts/build-and-push.sh $IMAGE_TAG

# Step 2: Start Kubernetes
echo "Step 2: Starting Kubernetes..."
./scripts/start-k8s-local.sh $IMAGE_TAG

echo ""
echo "🎉 Deployment complete!"
echo "======================"
echo "Frontend: http://$(minikube ip):30000"
echo "API Gateway: http://$(minikube ip):30001"
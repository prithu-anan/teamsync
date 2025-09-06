#!/bin/bash
set -e

# Configuration
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"prithuanan"}
IMAGE_TAG=${1:-"latest"}

echo "🔄 Updating Kubernetes deployments with new images"
echo "================================================="
echo "Docker Hub Username: $DOCKER_HUB_USERNAME"
echo "Image Tag: $IMAGE_TAG"
echo ""

# Update image references in Kubernetes
echo "📝 Updating image references..."
kubectl set image deployment/auth-service auth-service=$DOCKER_HUB_USERNAME/teamsync-auth-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/user-management-service user-management-service=$DOCKER_HUB_USERNAME/teamsync-user-management-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/project-management-service project-management-service=$DOCKER_HUB_USERNAME/teamsync-project-management-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/task-management-service task-management-service=$DOCKER_HUB_USERNAME/teamsync-task-management-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/message-management-service message-management-service=$DOCKER_HUB_USERNAME/teamsync-message-management-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/feed-management-service feed-management-service=$DOCKER_HUB_USERNAME/teamsync-feed-management-service:$IMAGE_TAG -n teamsync
kubectl set image deployment/api-gateway api-gateway=$DOCKER_HUB_USERNAME/teamsync-api-gateway:$IMAGE_TAG -n teamsync
kubectl set image deployment/ai-backend ai-backend=$DOCKER_HUB_USERNAME/teamsync-ai-backend:$IMAGE_TAG -n teamsync
kubectl set image deployment/frontend frontend=$DOCKER_HUB_USERNAME/teamsync-frontend:$IMAGE_TAG -n teamsync

# Wait for rollout to complete
echo "⏳ Waiting for rollout to complete..."
kubectl rollout status deployment/auth-service -n teamsync
kubectl rollout status deployment/user-management-service -n teamsync
kubectl rollout status deployment/project-management-service -n teamsync
kubectl rollout status deployment/task-management-service -n teamsync
kubectl rollout status deployment/message-management-service -n teamsync
kubectl rollout status deployment/feed-management-service -n teamsync
kubectl rollout status deployment/api-gateway -n teamsync
kubectl rollout status deployment/ai-backend -n teamsync
kubectl rollout status deployment/frontend -n teamsync

echo ""
echo "✅ All deployments updated successfully!"
echo "======================================"
echo "New image tag: $IMAGE_TAG"
echo "To check status: kubectl get pods -n teamsync"
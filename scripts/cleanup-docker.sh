#!/bin/bash

# Script to stop and remove Docker containers and images
# Excludes the top 3 containers: onestopjustice-frontend, onestopjustice-backend, onestopjustice-mongodb

echo "Starting Docker cleanup process..."

# Define the containers to keep (top 3)
KEEP_CONTAINERS=("onestopjustice-frontend" "onestopjustice-backend" "onestopjustice-mongodb")

# Get all running containers
echo "Checking running containers..."
RUNNING_CONTAINERS=$(docker ps --format "table {{.Names}}" | tail -n +2)

# Get all containers (including stopped ones)
echo "Checking all containers..."
ALL_CONTAINERS=$(docker ps -a --format "table {{.Names}}" | tail -n +2)

# Function to check if container should be kept
should_keep_container() {
    local container_name=$1
    for keep in "${KEEP_CONTAINERS[@]}"; do
        if [[ "$container_name" == "$keep" ]]; then
            return 0
        fi
    done
    return 1
}

# Stop and remove containers (excluding the ones to keep)
echo "Stopping and removing containers..."
for container in $ALL_CONTAINERS; do
    if ! should_keep_container "$container"; then
        echo "Processing container: $container"
        
        # Stop container if it's running
        if docker ps --format "{{.Names}}" | grep -q "^$container$"; then
            echo "  Stopping container: $container"
            docker stop "$container"
        fi
        
        # Remove container
        echo "  Removing container: $container"
        docker rm "$container"
    else
        echo "  Keeping container: $container"
    fi
done

# Remove associated images (excluding the ones used by kept containers)
echo "Removing unused images..."
KEEP_IMAGES=("prithuanan/onestopjustice-frontend:latest" "prithuanan/onestopjustice-backend:latest" "mongo:7.0")

# Get all images
ALL_IMAGES=$(docker images --format "table {{.Repository}}:{{.Tag}}" | tail -n +2)

for image in $ALL_IMAGES; do
    should_keep=false
    for keep_image in "${KEEP_IMAGES[@]}"; do
        if [[ "$image" == "$keep_image" ]]; then
            should_keep=true
            break
        fi
    done
    
    if [[ "$should_keep" == false ]]; then
        echo "Removing image: $image"
        docker rmi "$image" 2>/dev/null || echo "  Could not remove image (may be in use): $image"
    else
        echo "Keeping image: $image"
    fi
done

# Clean up dangling images and unused resources
echo "Cleaning up dangling images and unused resources..."
docker image prune -f
docker system prune -f

echo "Docker cleanup completed!"
echo "Remaining containers:"
docker ps -a
echo "Remaining images:"
docker images

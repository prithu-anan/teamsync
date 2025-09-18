#!/usr/bin/env bash
set -euo pipefail

# Docker Cleanup Script for TeamSync
# This script helps free up disk space by cleaning up Docker resources

echo "🧹 Docker Cleanup Script for TeamSync"
echo "======================================"

# Function to show disk usage
show_disk_usage() {
    echo "📊 Current disk usage:"
    df -h
    echo ""
    echo "🐳 Docker system space usage:"
    docker system df
    echo ""
}

# Function to show what will be cleaned
show_cleanup_preview() {
    echo "🔍 Cleanup preview:"
    echo "Containers to remove: $(docker ps -aq | wc -l)"
    echo "Images to remove: $(docker images -q | wc -l)"
    echo "Volumes to remove: $(docker volume ls -q | wc -l)"
    echo "Networks to remove: $(docker network ls -q | wc -l)"
    echo ""
}

# Function for safe cleanup (keeps running containers)
safe_cleanup() {
    echo "🛡️  Performing SAFE cleanup (keeps running containers)..."
    
    # Stop and remove stopped containers
    echo "Stopping and removing stopped containers..."
    docker container prune -f
    
    # Remove unused images
    echo "Removing unused images..."
    docker image prune -a -f
    
    # Remove unused volumes (be careful - this will delete database data!)
    echo "⚠️  WARNING: This will remove unused volumes including database data!"
    read -p "Do you want to remove unused volumes? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker volume prune -f
    else
        echo "Skipping volume cleanup..."
    fi
    
    # Remove unused networks
    echo "Removing unused networks..."
    docker network prune -f
    
    # Clean build cache
    echo "Cleaning build cache..."
    docker builder prune -a -f
    
    echo "✅ Safe cleanup completed!"
}

# Function for aggressive cleanup (removes everything)
aggressive_cleanup() {
    echo "⚠️  Performing AGGRESSIVE cleanup (removes ALL Docker data)..."
    echo "This will delete ALL containers, images, volumes, and networks!"
    read -p "Are you sure? This action cannot be undone! (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Cleanup cancelled."
        exit 0
    fi
    
    # Stop all containers
    echo "Stopping all containers..."
    docker stop $(docker ps -q) 2>/dev/null || true
    
    # Remove all containers
    echo "Removing all containers..."
    docker rm $(docker ps -aq) 2>/dev/null || true
    
    # Remove all images
    echo "Removing all images..."
    docker rmi $(docker images -q) 2>/dev/null || true
    
    # Remove all volumes
    echo "Removing all volumes..."
    docker volume rm $(docker volume ls -q) 2>/dev/null || true
    
    # Remove all networks
    echo "Removing all networks..."
    docker network rm $(docker network ls -q) 2>/dev/null || true
    
    # Clean build cache
    echo "Cleaning build cache..."
    docker builder prune -a -f
    
    # System prune
    echo "Performing system prune..."
    docker system prune -a -f --volumes
    
    echo "✅ Aggressive cleanup completed!"
}

# Function for database-specific cleanup
database_cleanup() {
    echo "🗄️  Performing database-specific cleanup..."
    
    # Stop database containers
    echo "Stopping database containers..."
    docker stop auth-postgres user-management-postgres project-management-postgres task-management-postgres feed-management-postgres message-management-postgres notification-postgres 2>/dev/null || true
    
    # Remove database containers
    echo "Removing database containers..."
    docker rm auth-postgres user-management-postgres project-management-postgres task-management-postgres feed-management-postgres message-management-postgres notification-postgres 2>/dev/null || true
    
    # Remove database volumes
    echo "Removing database volumes..."
    docker volume rm $(docker volume ls -q | grep -E "(postgres|db|data)") 2>/dev/null || true
    
    echo "✅ Database cleanup completed!"
}

# Main menu
main() {
    show_disk_usage
    show_cleanup_preview
    
    echo "Choose cleanup option:"
    echo "1) Safe cleanup (keeps running containers)"
    echo "2) Aggressive cleanup (removes everything)"
    echo "3) Database cleanup only"
    echo "4) Show disk usage only"
    echo "5) Exit"
    
    read -p "Enter your choice (1-5): " choice
    
    case $choice in
        1)
            safe_cleanup
            ;;
        2)
            aggressive_cleanup
            ;;
        3)
            database_cleanup
            ;;
        4)
            show_disk_usage
            exit 0
            ;;
        5)
            echo "Exiting..."
            exit 0
            ;;
        *)
            echo "Invalid choice. Exiting..."
            exit 1
            ;;
    esac
    
    echo ""
    show_disk_usage
    echo "🎉 Cleanup completed!"
}

# Run main function
main "$@"

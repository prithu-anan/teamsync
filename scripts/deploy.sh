#!/usr/bin/env bash
set -euo pipefail

# Deploy-only script: pulls images from Docker Hub and runs containers.
# Uses .env from each service directory if present. Does NOT build locally.

NETWORK_NAME="teamsync_network"

SERVICES=(
  "api-gateway:api_gateway:prithuanan/teamsync-api-gateway:latest:8080:8080"
  "auth-service:auth-service:prithuanan/teamsync-auth-service:latest:8081:8081"
  "user-management-service:user-management-service:prithuanan/teamsync-user-management-service:latest:8082:8082"
  "project-management-service:project-management-service:prithuanan/teamsync-project-management-service:latest:8083:8083"
  "task-management-service:task-management-service:prithuanan/teamsync-task-management-service:latest:8089:8089"
  "feed-management-service:feed-management-service:prithuanan/teamsync-feed-management-service:latest:8090:8090"
  "message-management-service:message-management-service:prithuanan/teamsync-message-management-service:latest:8091:8091"
  "notification-service:notification-service:prithuanan/teamsync-notification-service:latest:8092:8092"
  "teamsync-ai-backend:ai-backend:prithuanan/teamsync-ai-backend:latest:8000:8000"
  "teamsync-social-flow:frontend:prithuanan/teamsync-social-flow:${TAG:-latest}:3000:80"
)

ensure_network() {
  if ! docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}$"; then
    docker network create ${NETWORK_NAME}
  fi
}

stop_and_remove() {
  local container_name="$1"
  if docker ps -a --format '{{.Names}}' | grep -q "^${container_name}$"; then
    echo "Stopping container ${container_name}..."
    docker stop "${container_name}" || true
    echo "Removing container ${container_name}..."
    docker rm "${container_name}" || true
  fi
}

run_service() {
  local dir="$1"; shift
  local container_name="$1"; shift
  local image="$1"; shift
  local host_port="$1"; shift
  local container_port="$1"; shift

  echo "Pulling ${image}..."
  docker pull "${image}"

  # Load env file if exists
  local env_args=()
  if [ -f "${dir}/.env" ]; then
    env_args=(--env-file "${dir}/.env")
  fi

  echo "Starting ${container_name} from ${image} on ${host_port}:${container_port}..."
  docker run -d --name "${container_name}" \
    --restart unless-stopped \
    --network ${NETWORK_NAME} \
    -p "${host_port}:${container_port}" \
    "${env_args[@]}" \
    "${image}"
}

main() {
  ensure_network

  for entry in "${SERVICES[@]}"; do
    IFS=":" read -r dir container_name image host_port container_port <<< "$entry"
    stop_and_remove "${container_name}"
    run_service "${dir}" "${container_name}" "${image}" "${host_port}" "${container_port}"
  done
}

main "$@"



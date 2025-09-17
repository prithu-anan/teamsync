#!/usr/bin/env bash
set -euo pipefail

# Build and deploy script: builds images locally and runs containers.
# Does NOT push to Docker Hub. Uses per-service .env when present.

NETWORK_NAME="teamsync_network"

SERVICES=(
  "api-gateway:api_gateway:api-gateway:8080:8080"
  "auth-service:auth-service:auth-service:8081:8081"
  "user-management-service:user-management-service:user-management-service:8082:8082"
  "project-management-service:project-management-service:project-management-service:8083:8083"
  "task-management-service:task-management-service:task-management-service:8089:8089"
  "feed-management-service:feed-management-service:feed-management-service:8090:8090"
  "message-management-service:message-management-service:message-management-service:8091:8091"
  "notification-service:notification-service:notification-service:8092:8092"
  "teamsync-ai-backend:ai-backend:teamsync-ai-backend:8000:8000"
  "teamsync-social-flow:frontend:teamsync-social-flow:3000:80"
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

build_image() {
  local dir="$1"
  local image="$2"
  echo "Building ${image} from ${dir}..."
  if [ "${dir}" = "teamsync-social-flow" ] && [ -f "${dir}/.env" ]; then
    # Export VITE_* variables from .env as build args
    set -o allexport
    source "${dir}/.env"
    set +o allexport
    docker build -t "${image}:latest" "${dir}" \
      --build-arg VITE_SUPABASE_URL="${VITE_SUPABASE_URL:-}" \
      --build-arg VITE_SUPABASE_ANON_KEY="${VITE_SUPABASE_ANON_KEY:-}" \
      --build-arg VITE_AZURE_BLOB_SAS_TOKEN="${VITE_AZURE_BLOB_SAS_TOKEN:-}" \
      --build-arg VITE_AZURE_BLOB_SAS_URL="${VITE_AZURE_BLOB_SAS_URL:-}" \
      --build-arg VITE_ZEGO_APP_ID="${VITE_ZEGO_APP_ID:-}" \
      --build-arg VITE_ZEGO_SERVER_SECRET="${VITE_ZEGO_SERVER_SECRET:-}" \
      --build-arg VITE_WEBSOCKET_URL="${VITE_WEBSOCKET_URL:-}" \
      --build-arg VITE_API_BASE_URL="${VITE_API_BASE_URL:-}" \
      --build-arg VITE_AI_BACKEND_URL="${VITE_AI_BACKEND_URL:-}"
  else
    docker build -t "${image}:latest" "${dir}"
  fi
}

run_service() {
  local dir="$1"; shift
  local container_name="$1"; shift
  local image="$1"; shift
  local host_port="$1"; shift
  local container_port="$1"; shift

  # Load env file if exists
  local env_args=()
  if [ -f "${dir}/.env" ]; then
    env_args=(--env-file "${dir}/.env")
  fi

  echo "Starting ${container_name} from ${image}:latest on ${host_port}:${container_port}..."
  docker run -d --name "${container_name}" \
    --restart unless-stopped \
    --network ${NETWORK_NAME} \
    -p "${host_port}:${container_port}" \
    "${env_args[@]}" \
    "${image}:latest"
}

main() {
  ensure_network

  for entry in "${SERVICES[@]}"; do
    IFS=":" read -r dir container_name image host_port container_port <<< "$entry"
    stop_and_remove "${container_name}"
    build_image "${dir}" "${image}"
    run_service "${dir}" "${container_name}" "${image}" "${host_port}" "${container_port}"
  done
}

main "$@"



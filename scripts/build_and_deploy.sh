#!/usr/bin/env bash
set -euo pipefail

# Build and deploy script: builds images locally and runs containers.
# Does NOT push to Docker Hub. Uses per-service .env when present.

NETWORK_NAME="teamsync_network"

SERVICES=(
  "api-gateway:api_gateway:prithuanan/api-gateway:8080:8080"
  "auth-service:auth-service:prithuanan/auth-service:8081:8081"
  "user-management-service:user-management-service:prithuanan/user-management-service:8082:8082"
  "project-management-service:project-management-service:prithuanan/project-management-service:8083:8083"
  "task-management-service:task-management-service:prithuanan/task-management-service:8089:8089"
  "feed-management-service:feed-management-service:prithuanan/feed-management-service:8090:8090"
  "message-management-service:message-management-service:prithuanan/message-management-service:8091:8091"
  "notification-service:notification-service:prithuanan/notification-service:8092:8092"
  "teamsync-ai-backend:ai-backend:prithuanan/teamsync-ai-backend:8000:8000"
  "teamsync-social-flow:frontend:prithuanan/teamsync-social-flow:3000:80"
)

ensure_network() {
  if ! docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}$"; then
    docker network create ${NETWORK_NAME}
  fi
}

setup_infrastructure() {
  echo "Setting up infrastructure services..."
  
  # Start database services first
  echo "Starting database services..."
  
  # Auth Service Database
  docker run -d \
    --name auth-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=auth-db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5431:5432 \
    -v auth-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # User Management Database
  docker run -d \
    --name user-management-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=user_management_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5432:5432 \
    -v user-management-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # Project Management Database
  docker run -d \
    --name project-management-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=project_management_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5433:5432 \
    -v project-management-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # Task Management Database
  docker run -d \
    --name task-management-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=task_management_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5434:5432 \
    -v task-management-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # Feed Management Database
  docker run -d \
    --name feed-management-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=feed_management_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5435:5432 \
    -v feed-management-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # Message Management Database
  docker run -d \
    --name message-management-postgres \
    --network ${NETWORK_NAME} \
    -e POSTGRES_DB=message_management_db \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=postgres \
    -p 5436:5432 \
    -v message-management-postgres_data:/var/lib/postgresql/data \
    postgres:15
  
  # Notification Service MongoDB
  docker run -d \
    --name notification-mongo \
    --network ${NETWORK_NAME} \
    -e MONGO_INITDB_DATABASE=notification_db \
    -p 27017:27017 \
    -v notification-mongo_data:/data/db \
    mongo:7.0
  
  # Kafka Infrastructure
  echo "Starting Kafka infrastructure..."
  
  # Zookeeper
  docker run -d \
    --name zookeeper \
    --network ${NETWORK_NAME} \
    -e ZOOKEEPER_CLIENT_PORT=2181 \
    -e ZOOKEEPER_TICK_TIME=2000 \
    -v zookeeper_data:/var/lib/zookeeper/data \
    -v zookeeper_logs:/var/lib/zookeeper/log \
    confluentinc/cp-zookeeper:7.4.0
  
  # Kafka Broker
  docker run -d \
    --name broker \
    --network ${NETWORK_NAME} \
    -p 29092:29092 \
    -e KAFKA_BROKER_ID=1 \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://broker:29092 \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
    -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
    -v kafka_data:/var/lib/kafka/data \
    confluentinc/cp-kafka:7.4.0
  
  # Schema Registry
  docker run -d \
    --name schema-registry \
    --network ${NETWORK_NAME} \
    -p 8085:8081 \
    -e SCHEMA_REGISTRY_HOST_NAME=schema-registry \
    -e SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=broker:29092 \
    -v schema-registry_data:/var/lib/schema-registry \
    confluentinc/cp-schema-registry:7.4.0
  
  echo "Waiting for infrastructure to be ready..."
  sleep 30
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
  elif [ "${dir}" = "teamsync-ai-backend" ] && [ -f "${dir}/.env" ]; then
    # Handle AI backend with environment variables
    set -o allexport
    source "${dir}/.env"
    set +o allexport
    docker build -t "${image}:latest" "${dir}" \
      --build-arg AUTH_DATABASE_URL="${AUTH_DATABASE_URL:-}" \
      --build-arg PROJECT_DATABASE_URL="${PROJECT_DATABASE_URL:-}" \
      --build-arg TASK_DATABASE_URL="${TASK_DATABASE_URL:-}" \
      --build-arg USER_DATABASE_URL="${USER_DATABASE_URL:-}" \
      --build-arg MESSAGE_DATABASE_URL="${MESSAGE_DATABASE_URL:-}" \
      --build-arg GEMINI_API_KEY="${GEMINI_API_KEY:-}" \
      --build-arg DEEPSEEK_API_KEY="${DEEPSEEK_API_KEY:-}" \
      --build-arg QUADRANT_URL="${QUADRANT_URL:-}" \
      --build-arg QUADRANT_API_KEY="${QUADRANT_API_KEY:-}" \
      --build-arg OPENAI_API_KEY="${OPENAI_API_KEY:-}" \
      --build-arg PROJECT_ID="${PROJECT_ID:-}" \
      --build-arg BASE_SERVER_URL="${BASE_SERVER_URL:-}"
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

  echo "Starting ${container_name} from ${image}:latest on ${host_port}:${container_port}..."
  
  # Set up service-specific environment variables based on CI/CD
  case "${container_name}" in
    "api_gateway")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        "${image}:latest"
      ;;
    "auth-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://auth-postgres:5432/auth-db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "user-management-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://user-management-postgres:5432/user_management_db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "project-management-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://project-management-postgres:5432/project_management_db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "task-management-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://task-management-postgres:5432/task_management_db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "feed-management-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://feed-management-postgres:5432/feed_management_db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "message-management-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://message-management-postgres:5432/message_management_db \
        -e SPRING_DATASOURCE_USERNAME=postgres \
        -e SPRING_DATASOURCE_PASSWORD=postgres \
        "${image}:latest"
      ;;
    "notification-service")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        -e SPRING_DATA_MONGODB_URI=mongodb://notification-mongo:27017/notification_db \
        -e SPRING_KAFKA_BOOTSTRAP_SERVERS=broker:29092 \
        -e SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL=http://schema-registry:8081 \
        -e SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL=http://schema-registry:8081 \
        "${image}:latest"
      ;;
    "ai-backend")
      # Load env file if exists for AI backend
      local env_args=()
      if [ -f "${dir}/.env" ]; then
        env_args=(--env-file "${dir}/.env")
      fi
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        "${env_args[@]}" \
        "${image}:latest"
      ;;
    "frontend")
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        -p "${host_port}:${container_port}" \
        "${image}:latest"
      ;;
    *)
      # Default case - load env file if exists
      local env_args=()
      if [ -f "${dir}/.env" ]; then
        env_args=(--env-file "${dir}/.env")
      fi
      docker run -d --name "${container_name}" \
        --restart unless-stopped \
        --network ${NETWORK_NAME} \
        -p "${host_port}:${container_port}" \
        "${env_args[@]}" \
        "${image}:latest"
      ;;
  esac
}

main() {
  ensure_network
  setup_infrastructure

  for entry in "${SERVICES[@]}"; do
    # Parse the service entry: dir:container_name:image:host_port:container_port
    # Use a more robust parsing method to handle image names with colons
    local dir=$(echo "$entry" | cut -d: -f1)
    local container_name=$(echo "$entry" | cut -d: -f2)
    local image=$(echo "$entry" | cut -d: -f3)
    local host_port=$(echo "$entry" | cut -d: -f4)
    local container_port=$(echo "$entry" | cut -d: -f5)
    
    stop_and_remove "${container_name}"
    build_image "${dir}" "${image}"
    run_service "${dir}" "${container_name}" "${image}" "${host_port}" "${container_port}"
  done
}

main "$@"



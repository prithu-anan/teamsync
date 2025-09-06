#!/bin/bash
set -e

echo "🔄 Running database migrations..."

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
kubectl wait --for=condition=ready pod -l app=auth-postgres -n teamsync --timeout=60s
kubectl wait --for=condition=ready pod -l app=user-postgres -n teamsync --timeout=60s
kubectl wait --for=condition=ready pod -l app=project-postgres -n teamsync --timeout=60s
kubectl wait --for=condition=ready pod -l app=task-postgres -n teamsync --timeout=60s
kubectl wait --for=condition=ready pod -l app=message-postgres -n teamsync --timeout=60s
kubectl wait --for=condition=ready pod -l app=feed-postgres -n teamsync --timeout=60s

echo "✅ All databases are ready!"

# Run migrations for each service
echo "🔄 Running auth service migrations..."
kubectl run flyway-auth --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://auth-postgres:5432/auth-db -user=postgres -password=password

echo "🔄 Running user service migrations..."
kubectl run flyway-user --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://user-postgres:5432/user_management_db -user=postgres -password=password

echo "🔄 Running project service migrations..."
kubectl run flyway-project --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://project-postgres:5432/project_management_db -user=postgres -password=password

echo "🔄 Running task service migrations..."
kubectl run flyway-task --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://task-postgres:5432/task_management_db -user=postgres -password=password

echo "🔄 Running message service migrations..."
kubectl run flyway-message --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://message-postgres:5432/message_management_db -user=postgres -password=password

echo "🔄 Running feed service migrations..."
kubectl run flyway-feed --image=flyway/flyway:latest --rm -i --restart=Never -n teamsync -- \
  flyway migrate -url=jdbc:postgresql://feed-postgres:5432/feed_management_db -user=postgres -password=password

echo "✅ All migrations completed successfully!"

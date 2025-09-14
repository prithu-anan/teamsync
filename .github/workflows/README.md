# TeamSync CI/CD Workflows

This directory contains GitHub Actions workflows for building, testing, and deploying the TeamSync microservices application.

## 🚀 Complete CI/CD Process: From Push to Deployment

### **Step 1: Code Push to Repository**
When you push code to the `main` or `master` branch, GitHub Actions automatically triggers the CI/CD pipeline.

### **Step 2: Build Phase (Parallel Jobs)**
The pipeline runs three build jobs simultaneously:

#### **Java Services Build** (`build-java-services`)
1. **Checkout code** from repository
2. **Set up JDK 21** with Maven caching
3. **Create Docker network** (`teamsync_network`)
4. **Database setup** (for auth-service):
   - Start PostgreSQL container
   - Wait for database health check
   - Run Flyway migrations
5. **Maven build** for each service:
   - Navigate to service directory
   - Make Maven wrapper executable
   - Run `./mvnw clean package -Dmaven.test.skip=true`
6. **Docker image build**:
   - Set up Docker Buildx
   - Login to Docker Hub
   - Extract metadata (tags, labels)
   - Build and push image with `latest` tag

#### **AI Backend Build** (`build-ai-backend`)
1. **Checkout code** and set up Python 3.11
2. **Docker image build**:
   - Build Python FastAPI application
   - Push with `latest` tag

#### **Frontend Build** (`build-frontend`)
1. **Checkout code** and set up Node.js
2. **Docker image build**:
   - Two-stage build (Node.js build + Nginx serve)
   - Pass environment variables as build arguments
   - Build React application with Vite
   - Create optimized Nginx image
   - Push with `latest` tag

### **Step 3: Deploy Phase** (`deploy`)
After all builds complete successfully:

#### **VM Connection & Setup**
1. **SSH into VM** using provided credentials
2. **Create Docker network** if it doesn't exist
3. **Pull all images** with fallback strategy:
   - Pull `latest` tag for all services
   - Continue with warnings if pull fails

#### **Container Management**
1. **Stop existing containers** gracefully:
   - Check if containers exist before stopping
   - Stop and remove containers by name
2. **Resolve port conflicts**:
   - Kill any containers using required ports
   - Prevent deployment conflicts

#### **Database Setup**
Start 7 database containers:
- **6 PostgreSQL databases** (ports 5431-5436)
- **1 MongoDB database** (port 27017)
- Each service gets its dedicated database

#### **Service Deployment**
Start all application containers with:
- **Environment variables** for configuration
- **Volume mounts** for persistent data
- **Network connectivity** for inter-service communication
- **Port mapping** for external access

### **Step 4: Verification**
- **Container status check** (`docker ps`)
- **Service health verification**
- **Network connectivity confirmation**

## Workflows

### 1. `ci-cd.yml` - Full CI/CD Pipeline
**Triggers:** Push to `main` or `master` branches
**Purpose:** Complete build, push, and deploy pipeline

This workflow:
- Builds all Java services (Spring Boot microservices)
- Builds the AI backend (Python/FastAPI)
- Builds the frontend (React/Node.js)
- Pushes all images to Docker Hub
- Deploys all services to your VM via SSH

### 2. `build-only.yml` - Build and Push Only
**Triggers:** Pull Requests, Manual trigger only
**Purpose:** Build and push images without deployment

This workflow:
- Builds all services
- Pushes images to Docker Hub
- Does NOT deploy to VM
- Useful for testing builds on PRs or manual testing

### 3. `deploy-only.yml` - Deploy Only
**Triggers:** Manual trigger, Push to `deploy` branch
**Purpose:** Deploy existing images to VM

This workflow:
- Pulls latest images from Docker Hub
- Deploys all services to your VM
- Useful for deploying without rebuilding

## 📋 Required GitHub Secrets

### **Docker Hub Credentials**
| Secret | Description | Example |
|--------|-------------|---------|
| `REGISTRY_USER` | Docker Hub username | `your-dockerhub-username` |
| `REGISTRY_PASS` | Docker Hub password or access token | `your-dockerhub-password` |

### **VM Access Credentials**
| Secret | Description | Example |
|--------|-------------|---------|
| `VM_HOST` | VM IP address or hostname | `192.168.1.100` or `your-vm.com` |
| `VM_USER` | SSH username for VM | `ubuntu` or `root` |
| `SSH_KEY` | Private SSH key for VM access | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### **Application Secrets**
| Secret | Description | Used By |
|--------|-------------|---------|
| `JWT_SECRET` | JWT signing secret | All Java services |
| `GEMINI_API_KEY` | Google Gemini API key | AI Backend |
| `DEEPSEEK_API_KEY` | DeepSeek API key | AI Backend |
| `QUADRANT_URL` | Quadrant API URL | AI Backend |
| `QUADRANT_API_KEY` | Quadrant API key | AI Backend |
| `OPENAI_API_KEY` | OpenAI API key | AI Backend |
| `PROJECT_ID` | Project identifier | AI Backend |
| `BASE_SERVER_URL` | Base server URL | AI Backend |
| `FIREBASE_SA_KEY` | Firebase service account key (JSON) | AI Backend |

### **AI Backend Database URLs** (Automatically configured)
| Environment Variable | Description | Database |
|---------------------|-------------|----------|
| `AUTH_DATABASE_URL` | Authentication database | `localhost:5431` |
| `PROJECT_DATABASE_URL` | Project management database | `localhost:5433` |
| `TASK_DATABASE_URL` | Task management database | `localhost:5434` |
| `USER_DATABASE_URL` | User management database | `localhost:5432` |
| `MESSAGE_DATABASE_URL` | Message management database | `localhost:5436` |

### **Azure Storage Secrets**
| Secret | Description | Used By |
|--------|-------------|---------|
| `AZURE_CONNECTION_STRING` | Azure Storage connection string | User, Feed, Message services |
| `AZURE_CONTAINER_NAME` | Azure Blob container name | User, Feed, Message services |
| `AZURE_ACCOUNT_NAME` | Azure Storage account name | User, Feed, Message services |
| `AZURE_SAS_TOKEN` | Azure Storage SAS token | User, Feed, Message services |

### **Frontend Environment Variables** (Build-time)
| Secret | Description | Used By |
|--------|-------------|---------|
| `VITE_SUPABASE_URL` | Supabase project URL | Frontend build |
| `VITE_SUPABASE_ANON_KEY` | Supabase anonymous key | Frontend build |
| `VITE_AZURE_BLOB_SAS_TOKEN` | Azure Blob SAS token | Frontend build |
| `VITE_AZURE_BLOB_SAS_URL` | Azure Blob SAS URL | Frontend build |
| `VITE_ZEGO_APP_ID` | Zego video calling app ID | Frontend build |
| `VITE_ZEGO_SERVER_SECRET` | Zego server secret | Frontend build |
| `VITE_WEBSOCKET_URL` | WebSocket connection URL | Frontend build |

## 🏗️ Services Architecture

### **Java Services (Spring Boot)**
| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| API Gateway | 8080 | - | Route requests, authentication |
| Auth Service | 8081 | PostgreSQL (5431) | User authentication, JWT |
| User Management | 8082 | PostgreSQL (5432) | User profiles, Azure storage |
| Project Management | 8083 | PostgreSQL (5433) | Project CRUD operations |
| Task Management | 8089 | PostgreSQL (5434) | Task management |
| Feed Management | 8090 | PostgreSQL (5435) | Social feed, Azure storage |
| Message Management | 8091 | PostgreSQL (5436) | Real-time messaging |
| Notification Service | 8092 | MongoDB (27017) | Push notifications, Kafka |

### **AI Backend (Python FastAPI)**
| Service | Port | Databases | Purpose |
|---------|------|-----------|---------|
| AI Backend | 8000 | Multiple PostgreSQL (5432-5436) | AI features, RAG, LLM integration with microservices data |

### **Frontend (React + Nginx)**
| Service | Port | Purpose |
|---------|------|---------|
| Frontend | 3000 | User interface, static files |

## 🗄️ Database Configuration

### **PostgreSQL Databases** (6 instances)
- **Auth DB**: Port 5431 (auth-service)
- **User DB**: Port 5432 (user-management-service)
- **Project DB**: Port 5433 (project-management-service)
- **Task DB**: Port 5434 (task-management-service)
- **Feed DB**: Port 5435 (feed-management-service)
- **Message DB**: Port 5436 (message-management-service)

### **MongoDB** (1 instance)
- **Notification DB**: Port 27017 (notification-service)

### **Kafka Infrastructure** (3 services)
- **Zookeeper**: Port 2181 (Kafka coordination)
- **Kafka Broker**: Port 29092 (Message streaming)
- **Schema Registry**: Port 8085 (Avro schema management)

## 🤖 AI Backend Multi-Database Architecture

The AI Backend connects to multiple microservice databases to provide intelligent features:

### **Database Connections**
- **Auth Database** (`localhost:5431`): Accesses authentication data for JWT validation and user sessions
- **Project Database** (`localhost:5433`): Accesses project and task data for AI-powered task estimation
- **User Database** (`localhost:5432`): Accesses user profiles for personalized AI responses
- **Message Database** (`localhost:5436`): Accesses channels and messages for auto-reply suggestions

### **AI Features**
- **Task Estimation**: Analyzes project context and similar tasks to estimate priority and time
- **Auto-Reply**: Generates contextual response suggestions based on conversation history
- **Smart Recommendations**: Uses data from all microservices for comprehensive AI insights

### **Data Flow**
```
AI Backend → localhost:5431 (Auth data)
          → localhost:5432 (User data)
          → localhost:5433 (Project data)  
          → localhost:5434 (Task data)
          → localhost:5436 (Message data)
```

## 🌐 Network Configuration

### **Docker Network**
- **Network Name**: `teamsync_network`
- **Purpose**: Internal service communication
- **Services**: All backend services (Java + AI)

### **Frontend Communication**
The frontend connects to backend services via **exposed ports** on localhost:
- **API Calls**: `http://localhost:8080` (API Gateway)
- **Message WebSocket**: `http://localhost:8091/ws` (Message Service)
- **Notification WebSocket**: `http://localhost:8092/ws` (Notification Service)

### **Backend Communication**
Backend services communicate internally using **container names**:
- **API Gateway**: `http://auth-service:8081`
- **Service URLs**: Hardcoded in `application.properties` files

## 🚦 Workflow Triggers

### **1. `ci-cd.yml` - Complete Pipeline**
- **Triggers**: Push to `main` or `master` branches
- **Purpose**: Full build and deployment
- **Duration**: ~10-15 minutes

### **2. `build-only.yml` - Build Only**
- **Triggers**: Pull requests, manual trigger
- **Purpose**: Build and push images without deployment
- **Duration**: ~8-12 minutes

### **3. `deploy-only.yml` - Deploy Only**
- **Triggers**: Manual trigger, push to `deploy` branch
- **Purpose**: Deploy existing images to VM
- **Duration**: ~3-5 minutes

## 🔧 Key Features

### **Error Handling & Resilience**
- **Graceful image pulling**: Pull `latest` tag with warning on failure
- **Graceful container management**: Check existence before operations
- **Port conflict resolution**: Kill conflicting containers
- **Health checks**: Database and service health monitoring

### **Build Optimizations**
- **Maven caching**: Faster Java builds
- **Docker layer caching**: Faster image builds
- **Multi-platform builds**: `linux/amd64` specification
- **Parallel builds**: All services build simultaneously

### **Deployment Features**
- **Zero-downtime deployment**: Graceful container replacement
- **Database migrations**: Flyway integration for auth-service
- **Environment separation**: Build-time vs runtime variables
- **Network isolation**: Secure inter-service communication

## 📝 Usage Examples

### **Automatic Deployment**
```bash
git add .
git commit -m "Add new feature"
git push origin main
# GitHub Actions automatically builds and deploys
```

### **Manual Build Only**
1. Go to GitHub Actions tab
2. Select "Build and Push Images Only"
3. Click "Run workflow"
4. Select branch and run

### **Manual Deploy Only**
1. Go to GitHub Actions tab
2. Select "Deploy to VM"
3. Click "Run workflow"
4. Select branch and run

### **Deploy Branch Trigger**
```bash
git checkout -b deploy
git push origin deploy
# Triggers deploy-only workflow
```

## ⚠️ Important Notes

- **Backend service URLs** are hardcoded in `application.properties` files
- **Frontend environment variables** are used at build time, not runtime
- **AI Backend database URLs** are automatically configured in workflows (no secrets needed)
- **Kafka and Schema Registry** are expected for the notification service
- **All services** use the same Docker network for internal communication
- **Database connections** are configured per service in their respective `application.properties`
- **Azure Storage** is used by user, feed, and message services for file storage
- **AI Backend** connects to multiple microservice databases via exposed ports

## 🔍 Troubleshooting

### **Build Failures**
1. Check build logs for specific service errors
2. Verify Maven dependencies and Java version
3. Check Docker build context and Dockerfile syntax

### **Deployment Failures**
1. Verify SSH access and VM connectivity
2. Check if all required secrets are configured
3. Ensure VM has sufficient resources (CPU, memory, disk)

### **Service Issues**
1. Check container logs: `docker logs <container_name>`
2. Verify database connectivity and health
3. Check network connectivity between services

### **Database Issues**
1. Ensure PostgreSQL containers are running and healthy
2. Check database connection strings in application.properties
3. Verify Flyway migrations completed successfully

### **Frontend Issues**
1. Check if environment variables are properly set during build
2. Verify Nginx configuration and static file serving
3. Check browser console for JavaScript errors

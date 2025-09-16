from fastapi import FastAPI
from app.routes import health, task, channel, chatbot
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="TeamSync AI Backend",
    description="Backend API for TeamSync AI with RAG-powered chatbot and microservice integration",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router, prefix="/api")
app.include_router(task.router, prefix="/api")
app.include_router(channel.router, prefix="/api")
app.include_router(chatbot.router, prefix="/api")

@app.get("/")
async def root():
    return {
        "message": "TeamSync AI Backend",
        "version": "2.0.0",
        "status": "running",
        "architecture": "microservices"
    }
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from app.agent.forum_agent import ForumAgent
from app.memory.chat_memory import clear_session_history
from app.config import settings

app = FastAPI(title=settings.app_name, version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)
agent = ForumAgent()


class AskRequest(BaseModel):
    query: str
    user_id: str


class AskResponse(BaseModel):
    answer: str
    user_id: str


@app.get("/")
async def root():
    return {
        "service": settings.app_name,
        "version": "1.0.0",
        "tools": ["get_hot_posts", "summarize_post", "check_content"],
    }


@app.post("/api/ai/ask", response_model=AskResponse)
async def ask_ai(request: AskRequest):
    try:
        result = await agent.ask(request.query, request.user_id)
        return AskResponse(answer=result["answer"], user_id=request.user_id)
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.delete("/api/ai/session/{user_id}")
async def delete_session(user_id: str):
    clear_session_history(user_id)
    return {"message": "会话已清空"}


@app.get("/api/ai/health")
async def health_check():
    return {"status": "healthy"}

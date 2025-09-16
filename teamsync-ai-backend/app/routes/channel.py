from fastapi import APIRouter, Depends, HTTPException, Header
from pydantic import BaseModel
from typing import Optional, List, Union
from app.clients import MessageClient, UserClient
from app.deps import get_message_client, get_user_client
from app.llm.factory import get_llm_provider
import json
import re

router = APIRouter()
llm = get_llm_provider("gemini")

class ChannelAutoReplyRequest(BaseModel):
    channel_id: int
    sender_id: int
    parent_thread_id: Optional[int] = None

class DirectMessageAutoReplyRequest(BaseModel):
    recipient_id: int
    sender_id: int
    parent_thread_id: Optional[int] = None

class ReplySuggestion(BaseModel):
    reply: str
    tone: str

class AutoReplyResponse(BaseModel):
    suggestions: List[ReplySuggestion]

def construct_prompt(messages: List[dict], sender: dict, recipient: Optional[dict] = None) -> str:
    prompt = """You are an expert communication assistant helping users craft quick replies in messaging channels. Your task is to generate 3 relevant, concise response suggestions for the sender based on the conversation context.

### Sender Information:
Name: {sender_name}
Role: {sender_role}

### Conversation Context:
""".format(
        sender_name=sender["name"], 
        sender_role=sender.get("designation", "Not specified")
    )
    
    for message in reversed(messages[-10:]):  # Last 10 messages, oldest first
        sender_name = message.get("senderName", "Unknown")
        prompt += f"{sender_name} ({message.get('timestamp', 'Unknown time')}): {message.get('content', '')}\n"

    if recipient:
        prompt += f"\n### Recipient Information:\nName: {recipient['name']}\nRole: {recipient.get('designation', 'Not specified')}\n"

    prompt += """
### Instructions:
1. Analyze the conversation flow and tone
2. Generate 3 distinct reply options that would be appropriate next responses for the sender
3. Vary the suggestions to cover different possible intents:
   - One straightforward continuation
   - One more detailed/expanded response
   - One alternative approach (question, humorous, etc.)
4. Keep suggestions brief (1-2 sentences max)
5. Match the tone of the existing conversation
6. Format response as JSON:
```json
[
    {"reply": "suggestion", "tone": "formal"},
    {"reply": "suggestion", "tone": "diplomatic"},
    {"reply": "suggestion", "tone": "friendly"}
]
```"""
    return prompt

def parse_llm_response(response: str) -> List[ReplySuggestion]:
    try:
        cleaned_response = re.sub(
            r"```json\n|\n```", "", response, flags=re.MULTILINE
        ).strip()
        suggestions = json.loads(cleaned_response)
        return [ReplySuggestion(reply=s["reply"], tone=s["tone"]) for s in suggestions]
    except json.JSONDecodeError as e:
        raise HTTPException(
            status_code=500, detail=f"Failed to parse LLM response: {str(e)}"
        )

@router.post("/channels/auto-reply", response_model=AutoReplyResponse)
async def auto_reply(
    request: Union[ChannelAutoReplyRequest, DirectMessageAutoReplyRequest],
    authorization: str = Header(..., description="JWT token in format: Bearer <token>"),
    message_client: MessageClient = Depends(get_message_client),
    user_client: UserClient = Depends(get_user_client)
):
    # Extract JWT token
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header format")
    
    jwt_token = authorization[7:]
    
    channel_id = getattr(request, "channel_id", None)
    recipient_id = getattr(request, "recipient_id", None)
    sender_id = request.sender_id

    # Validate input
    if channel_id and recipient_id:
        raise HTTPException(
            status_code=400, detail="Cannot specify both channel_id and recipient_id"
        )
    if not (channel_id or recipient_id):
        raise HTTPException(
            status_code=400, detail="Must specify either channel_id or recipient_id"
        )

    # Validate sender exists
    sender = await user_client.get_user_by_id(sender_id)
    if not sender:
        raise HTTPException(status_code=404, detail="Sender not found")

    # Validate channel or recipient
    if channel_id:
        channel = await message_client.get_channel_by_id(channel_id, jwt_token)
        if not channel:
            raise HTTPException(status_code=404, detail="Channel not found")
    
    recipient = None
    if recipient_id:
        recipient = await user_client.get_user_by_id(recipient_id)
        if not recipient:
            raise HTTPException(status_code=404, detail="Recipient not found")

    # Fetch conversation context
    messages = []
    if channel_id:
        messages = await message_client.get_channel_messages(channel_id, jwt_token)

    # Construct LLM prompt
    prompt = construct_prompt(messages, sender, recipient)
    print(f"prompt: {prompt}")

    # Get LLM response
    try:
        responses = llm.generate(prompt)
        print(f"llm said: {responses[0] if responses else 'No response'}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM request failed: {str(e)}")

    # Parse response
    suggestions = parse_llm_response(responses[0] if responses else "[]")

    return AutoReplyResponse(suggestions=suggestions)
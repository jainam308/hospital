from dotenv import load_dotenv
import os

from langchain_groq import ChatGroq
from langchain_core.messages import SystemMessage, HumanMessage

from state import PatientState

# Load environment variables from .env
load_dotenv()

# Read API Key
api_key = os.getenv("GROQ_API_KEY")

# Check if API Key exists
if api_key is None:
    raise ValueError("GROQ_API_KEY not found in .env file")

# Explicitly set it in the environment
os.environ["GROQ_API_KEY"] = api_key

# Create LLM
llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    temperature=0
)

# System Prompt
ROUTER_SYSTEM_PROMPT = """
You are a hospital triage router.

Based on the patient's symptoms, classify them into EXACTLY ONE of these wards:

- emergency: life-threatening or urgent physical conditions
- mental_health: psychological or emotional distress
- general: all other non-urgent conditions

Respond with ONLY one word:
emergency
mental_health
general
"""

# Safety keywords
EMERGENCY_KEYWORDS = [
    "chest pain",
    "can't breathe",
    "unconscious",
    "severe bleeding",
    "heart attack"
]

CRISIS_KEYWORDS = [
    "suicide",
    "kill myself",
    "self-harm",
    "want to die"
]


def router_node(state: PatientState) -> PatientState:

    query_lower = state["query"].lower()

    # Crisis Guardrail
    if any(keyword in query_lower for keyword in CRISIS_KEYWORDS):
        ward = "mental_health"
        reasoning = "Crisis keyword detected."

    # Emergency Guardrail
    elif any(keyword in query_lower for keyword in EMERGENCY_KEYWORDS):
        ward = "emergency"
        reasoning = "Emergency keyword detected."

    # LLM Classification
    else:
        response = llm.invoke(
            [
                SystemMessage(content=ROUTER_SYSTEM_PROMPT),
                HumanMessage(
                    content=f"Age: {state['age']}\nSymptoms: {state['query']}"
                ),
            ]
        )

        result = response.content.strip().lower()

        if "emergency" in result:
            ward = "emergency"

        elif "mental_health" in result or "mental health" in result:
            ward = "mental_health"

        elif "general" in result:
            ward = "general"

        else:
            ward = "general"

        reasoning = f"LLM classified patient based on symptoms: {state['query']}"

    return {
        **state,
        "ward": ward,
        "reasoning": reasoning,
    }
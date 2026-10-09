from typing import Literal

from langgraph.graph import StateGraph, END

from state import PatientState
from intake import intake_node
from router import router_node
from wards import (
    general_ward_node,
    emergency_ward_node,
    mental_health_ward_node,
)
from doctor import doctor_availability_node


# Decide which ward node to execute
def route_decision(state: PatientState) -> Literal["general", "emergency", "mental_health"]:
    return state["ward"]


# Create graph
builder = StateGraph(PatientState)

# Add nodes
builder.add_node("intake", intake_node)
builder.add_node("router", router_node)
builder.add_node("general", general_ward_node)
builder.add_node("emergency", emergency_ward_node)
builder.add_node("mental_health", mental_health_ward_node)
builder.add_node("doctor_check", doctor_availability_node)

# Set entry point
builder.set_entry_point("intake")

# Connect nodes
builder.add_edge("intake", "router")

# Conditional routing
builder.add_conditional_edges(
    "router",
    route_decision,
    {
        "general": "general",
        "emergency": "emergency",
        "mental_health": "mental_health",
    },
)

# Connect all ward nodes to doctor node
builder.add_edge("general", "doctor_check")
builder.add_edge("emergency", "doctor_check")
builder.add_edge("mental_health", "doctor_check")

# End the workflow
builder.add_edge("doctor_check", END)

# Compile graph
graph = builder.compile()

# Run graph
result = graph.invoke({})

print("\n===== FINAL PATIENT STATE =====")
print(result)
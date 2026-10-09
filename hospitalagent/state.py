from typing import TypedDict


class PatientState(TypedDict):
    name: str
    age: str
    query: str
    ward: str
    reasoning: str
    assigned_doctor: str
    next_slot: str
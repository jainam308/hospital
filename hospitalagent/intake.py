from state import PatientState


def intake_node(state: PatientState) -> PatientState:

    print("=== Patient Intake Form ===")

    name = input("Patient Name: ")
    age = input("Patient Age: ")
    query = input("Symptoms: ")

    return {
        "name": name,
        "age": age,
        "query": query,
        "ward": "",
        "reasoning": "",
        "assigned_doctor": "",
        "next_slot": ""
    }
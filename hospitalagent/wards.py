from state import PatientState


def general_ward_node(state: PatientState) -> PatientState:
    print("\n========== GENERAL WARD ==========")
    print(f"Patient Name : {state['name']}")
    print(f"Age          : {state['age']}")
    print(f"Symptoms     : {state['query']}")
    print(f"Ward         : {state['ward']}")
    print(f"Reason       : {state['reasoning']}")

    return state


def emergency_ward_node(state: PatientState) -> PatientState:
    print("\n========== EMERGENCY WARD ==========")
    print(f"Patient Name : {state['name']}")
    print(f"Age          : {state['age']}")
    print(f"Symptoms     : {state['query']}")
    print(f"Ward         : {state['ward']}")
    print(f"Reason       : {state['reasoning']}")
    print("Priority     : HIGH")

    return state


def mental_health_ward_node(state: PatientState) -> PatientState:
    print("\n====== MENTAL HEALTH WARD ======")
    print(f"Patient Name : {state['name']}")
    print(f"Age          : {state['age']}")
    print(f"Symptoms     : {state['query']}")
    print(f"Ward         : {state['ward']}")
    print(f"Reason       : {state['reasoning']}")

    return state
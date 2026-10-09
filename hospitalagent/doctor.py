import os
import pandas as pd
from datetime import datetime, timedelta
from state import PatientState

# Robust path to doctors.csv
CSV_PATH = os.path.join(os.path.dirname(__file__), "doctors.csv")
doctors_df = pd.read_csv(CSV_PATH)


def doctor_availability_node(state: PatientState) -> PatientState:

    ward = state["ward"]

    # Find active doctors in the required ward
    available = doctors_df[
        (doctors_df["ward"].str.lower() == ward.lower()) &
        (doctors_df["status"].str.lower() == "active")
    ]

    if not available.empty:

        # Sort doctors by earliest available slot
        available = available.sort_values(by="next_slot")

        # Select the first doctor
        doctor = available.iloc[0]

        assigned_doctor = doctor["doctor_name"]
        assigned_slot = doctor["next_slot"]
        slot_minutes = int(doctor["slot_minutes"])

        print(f"\n👨‍⚕️ Doctor assigned : {assigned_doctor}")
        print(f"🕒 Appointment Time : {assigned_slot}")

        # Calculate next available slot
        current_time = datetime.strptime(assigned_slot, "%H:%M")
        next_time = current_time + timedelta(minutes=slot_minutes)
        updated_slot = next_time.strftime("%H:%M")

        # Update doctor's next slot
        doctors_df.loc[
            doctors_df["doctor_name"] == assigned_doctor,
            "next_slot"
        ] = updated_slot

        # Save updated CSV
        doctors_df.to_csv(CSV_PATH, index=False)

    else:

        assigned_doctor = "No active doctor available"
        assigned_slot = "N/A"

        print("\n👨‍⚕️ No active doctor available.")

    return {
        **state,
        "assigned_doctor": assigned_doctor,
        "next_slot": assigned_slot
    }
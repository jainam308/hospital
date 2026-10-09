export interface PrescriptionItem {
  id?: number;
  medicineName: string;
  dosage: string;
  frequency: string;
  duration: string;
  instructions?: string;
}

export interface ConsultationRequest {
  appointmentId: number;
  bloodPressure: string;
  heartRate?: number;
  temperature?: number;
  notes: string;
  prescriptionItems?: PrescriptionItem[];
}

export interface ConsultationResponse {
  id: number;
  appointmentId: number;
  patientId: number;
  patientName: string;
  doctorName: string;
  bloodPressure: string;
  heartRate?: number;
  temperature?: number;
  notes: string;
  patientBloodGroup?: string;
  patientAllergies?: string;
  patientChronicConditions?: string;
  prescriptionItems?: PrescriptionItem[];
  consultationDate: string;
}

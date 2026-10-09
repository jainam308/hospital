export interface ConsultationRequest {
  appointmentId: number;
  bloodPressure: string;
  heartRate?: number;
  temperature?: number;
  notes: string;
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
  consultationDate: string;
}

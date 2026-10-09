export type AppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';

export interface AppointmentRequest {
  patientId: number;
  doctorName: string;
  appointmentDateTime: string; // ISO 8601 string
}

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  patientPhone: string;
  doctorName: string;
  appointmentDateTime: string;
  status: AppointmentStatus;
  createdAt: string;
}

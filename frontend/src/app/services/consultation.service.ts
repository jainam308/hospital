import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConsultationRequest, ConsultationResponse } from '../models/consultation.model';

@Injectable({
  providedIn: 'root'
})
export class ConsultationService {
  private apiUrl = 'http://localhost:8080/api/consultations';

  constructor(private http: HttpClient) {}

  createConsultation(request: ConsultationRequest): Observable<ConsultationResponse> {
    return this.http.post<ConsultationResponse>(this.apiUrl, request);
  }

  getConsultationsByPatient(patientId: number): Observable<ConsultationResponse[]> {
    return this.http.get<ConsultationResponse[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  getConsultationByAppointment(appointmentId: number): Observable<ConsultationResponse> {
    return this.http.get<ConsultationResponse>(`${this.apiUrl}/appointment/${appointmentId}`);
  }
}

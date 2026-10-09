import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppointmentRequest, AppointmentResponse } from '../models/appointment.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private apiUrl = 'http://localhost:8081/api/appointments';

  constructor(private http: HttpClient) {}

  getAllAppointments(): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(this.apiUrl);
  }

  getTodayAppointments(): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/today`);
  }

  getAppointmentsByPatient(patientId: number): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  bookAppointment(request: AppointmentRequest): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(this.apiUrl, request);
  }

  updateStatus(id: number, status: string): Observable<AppointmentResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<AppointmentResponse>(`${this.apiUrl}/${id}/status`, null, { params });
  }
}

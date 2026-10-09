import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Department, Doctor, SlotConflictResponse } from '../models/doctor.model';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private apiUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  getDoctors(departmentId?: number): Observable<Doctor[]> {
    let params = new HttpParams();
    if (departmentId) {
      params = params.set('departmentId', departmentId.toString());
    }
    return this.http.get<Doctor[]>(`${this.apiUrl}/doctors`, { params });
  }

  getDoctorById(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.apiUrl}/doctors/${id}`);
  }

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.apiUrl}/departments`);
  }

  checkSlotConflict(doctorName: string, appointmentTime: string): Observable<SlotConflictResponse> {
    const params = new HttpParams()
      .set('doctorName', doctorName)
      .set('appointmentTime', appointmentTime);
    return this.http.get<SlotConflictResponse>(`${this.apiUrl}/doctors/check-conflict`, { params });
  }
}

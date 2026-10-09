import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patient } from '../models/patient.model';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private apiUrl = 'http://localhost:8081/api/patients';

  constructor(private http: HttpClient) {}

  getPatients(query?: string): Observable<Patient[]> {
    let params = new HttpParams();
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Patient[]>(this.apiUrl, { params });
  }

  getPatientById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/${id}`);
  }

  createPatient(patient: Patient): Observable<Patient> {
    return this.http.post<Patient>(this.apiUrl, patient);
  }
}

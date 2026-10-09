import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PatientService } from '../../services/patient.service';
import { Patient } from '../../models/patient.model';

@Component({
  selector: 'app-patient',
  templateUrl: './patient.component.html',
  styleUrls: ['./patient.component.css']
})
export class PatientComponent implements OnInit {
  patients: Patient[] = [];
  patientForm!: FormGroup;
  searchQuery: string = '';
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  bloodGroups: string[] = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  existingPatientMatch: Patient | null = null;
  existingMatchField: 'phone' | 'email' | null = null;

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPatients();
  }

  initForm(): void {
    this.patientForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      gender: ['MALE', [Validators.required]],
      age: [null, [Validators.required, Validators.min(0), Validators.max(130)]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]{7,20}$/)]],
      email: ['', [Validators.email]],
      bloodGroup: [''],
      allergies: [''],
      chronicConditions: ['']
    });

    this.patientForm.valueChanges.subscribe(() => {
      this.checkDuplicateLive();
    });
  }

  checkDuplicateLive(): void {
    const rawPhone = this.patientForm.get('phoneNumber')?.value;
    const cleanPhone = rawPhone ? rawPhone.trim().replace(/[\s\-()]/g, '') : '';
    const cleanEmail = this.patientForm.get('email')?.value ? this.patientForm.get('email')?.value.trim().toLowerCase() : '';

    if (cleanPhone && cleanPhone.length >= 7) {
      const match = this.patients.find(p => p.phoneNumber && p.phoneNumber.replace(/[\s\-()]/g, '') === cleanPhone);
      if (match) {
        this.existingPatientMatch = match;
        this.existingMatchField = 'phone';
        return;
      }
    }

    if (cleanEmail && cleanEmail.length > 3) {
      const match = this.patients.find(p => p.email && p.email.trim().toLowerCase() === cleanEmail);
      if (match) {
        this.existingPatientMatch = match;
        this.existingMatchField = 'email';
        return;
      }
    }

    this.existingPatientMatch = null;
    this.existingMatchField = null;
  }

  loadPatients(): void {
    this.isLoading = true;
    this.patientService.getPatients(this.searchQuery).subscribe({
      next: (data) => {
        this.patients = data;
        this.isLoading = false;
        this.checkDuplicateLive();
      },
      error: () => {
        this.errorMessage = 'Failed to load patients from server.';
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.loadPatients();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadPatients();
  }

  onSubmit(): void {
    if (this.patientForm.invalid) {
      this.patientForm.markAllAsTouched();
      return;
    }

    if (this.existingPatientMatch) {
      this.errorMessage = `User already exists! A patient with this ${this.existingMatchField === 'phone' ? 'phone number' : 'email'} is already registered as "${this.existingPatientMatch.name}" (Patient ID #${this.existingPatientMatch.id}). Duplicate registration is not permitted.`;
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const newPatient: Patient = this.patientForm.value;
    this.patientService.createPatient(newPatient).subscribe({
      next: (created) => {
        this.successMessage = `Patient "${created.name}" registered successfully with ID #${created.id}!`;
        this.isSubmitting = false;
        this.resetForm();
        this.loadPatients();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error occurred while registering patient.';
        this.isSubmitting = false;
      }
    });
  }

  resetForm(): void {
    this.patientForm.reset({
      name: '',
      gender: 'MALE',
      age: null,
      phoneNumber: '',
      email: '',
      bloodGroup: '',
      allergies: '',
      chronicConditions: ''
    });
    this.existingPatientMatch = null;
    this.existingMatchField = null;
  }

  filterToPatient(patient: Patient): void {
    this.searchQuery = patient.phoneNumber || patient.name;
    this.loadPatients();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.patientForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  bookAppointment(patient: Patient): void {
    this.router.navigate(['/appointments'], {
      queryParams: { patientId: patient.id, patientName: patient.name }
    });
  }

  viewHistory(patient: Patient): void {
    this.router.navigate(['/consultations'], {
      queryParams: { patientId: patient.id }
    });
  }
}

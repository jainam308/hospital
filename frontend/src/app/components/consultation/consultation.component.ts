import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConsultationService } from '../../services/consultation.service';
import { AppointmentService } from '../../services/appointment.service';
import { PatientService } from '../../services/patient.service';
import { ConsultationRequest, ConsultationResponse } from '../../models/consultation.model';
import { AppointmentResponse } from '../../models/appointment.model';
import { Patient } from '../../models/patient.model';

@Component({
  selector: 'app-consultation',
  templateUrl: './consultation.component.html',
  styleUrls: ['./consultation.component.css']
})
export class ConsultationComponent implements OnInit {
  consultationForm!: FormGroup;
  scheduledAppointments: AppointmentResponse[] = [];
  selectedAppointment?: AppointmentResponse;
  patientList: Patient[] = [];
  consultations: ConsultationResponse[] = [];

  filterPatientId: number | null = null;
  isLoadingHistory: boolean = false;
  isSubmitting: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private consultationService: ConsultationService,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadScheduledAppointments();
    this.loadPatients();

    // Check query params if navigated with specific appointment or patient
    this.route.queryParams.subscribe(params => {
      if (params['patientId']) {
        this.filterPatientId = Number(params['patientId']);
        this.loadConsultationsForPatient(this.filterPatientId);
      }
      if (params['appointmentId']) {
        const apptId = Number(params['appointmentId']);
        this.consultationForm.patchValue({ appointmentId: apptId });
        this.onAppointmentSelect();
      }
    });
  }

  initForm(): void {
    this.consultationForm = this.fb.group({
      appointmentId: [null, [Validators.required]],
      bloodPressure: ['', [Validators.required]],
      heartRate: [null, [Validators.min(30), Validators.max(250)]],
      temperature: [null, [Validators.min(90), Validators.max(110)]],
      notes: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  loadPatients(): void {
    this.patientService.getPatients().subscribe({
      next: (data) => {
        this.patientList = data;
      }
    });
  }

  loadScheduledAppointments(): void {
    this.appointmentService.getAllAppointments().subscribe({
      next: (data) => {
        this.scheduledAppointments = data.filter(a => a.status === 'SCHEDULED');
        this.onAppointmentSelect();
      }
    });
  }

  onAppointmentSelect(): void {
    const apptId = this.consultationForm.get('appointmentId')?.value;
    if (apptId) {
      this.selectedAppointment = this.scheduledAppointments.find(a => a.id === Number(apptId));
      if (this.selectedAppointment && !this.filterPatientId) {
        this.filterPatientId = this.selectedAppointment.patientId;
        this.loadConsultationsForPatient(this.filterPatientId);
      }
    } else {
      this.selectedAppointment = undefined;
    }
  }

  onPatientFilterChange(): void {
    if (this.filterPatientId) {
      this.loadConsultationsForPatient(this.filterPatientId);
    } else {
      this.consultations = [];
    }
  }

  loadConsultationsForPatient(patientId: number): void {
    this.isLoadingHistory = true;
    this.consultationService.getConsultationsByPatient(patientId).subscribe({
      next: (data) => {
        this.consultations = data;
        this.isLoadingHistory = false;
      },
      error: () => {
        this.consultations = [];
        this.isLoadingHistory = false;
      }
    });
  }

  onSubmit(): void {
    if (this.consultationForm.invalid) {
      this.consultationForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const req: ConsultationRequest = this.consultationForm.value;

    this.consultationService.createConsultation(req).subscribe({
      next: (res) => {
        this.successMessage = `Consultation summary recorded successfully for ${res.patientName}! Appointment marked as COMPLETED.`;
        this.isSubmitting = false;

        const patientId = res.patientId;
        this.filterPatientId = patientId;
        this.loadConsultationsForPatient(patientId);

        // Reset form and reload pending appointments list
        this.initForm();
        this.selectedAppointment = undefined;
        this.loadScheduledAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to save consultation summary.';
        this.isSubmitting = false;
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.consultationForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}

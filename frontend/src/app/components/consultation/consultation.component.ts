import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ConsultationService } from '../../services/consultation.service';
import { AppointmentService } from '../../services/appointment.service';
import { PatientService } from '../../services/patient.service';
import { ConsultationRequest, ConsultationResponse, PrescriptionItem } from '../../models/consultation.model';
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
  selectedPatient?: Patient;
  patientList: Patient[] = [];
  consultations: ConsultationResponse[] = [];

  prescriptionItems: PrescriptionItem[] = [];

  filterPatientId: number | null = null;
  isLoadingHistory: boolean = false;
  isSubmitting: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';
  recentAppointmentId?: number;

  constructor(
    private fb: FormBuilder,
    private consultationService: ConsultationService,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPatients();
    this.loadScheduledAppointments();

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
    this.prescriptionItems = [];
    this.selectedPatient = undefined;
  }

  addPrescriptionRow(): void {
    this.prescriptionItems.push({
      medicineName: '',
      dosage: '',
      frequency: '1-0-1 (Twice daily)',
      duration: '5 days',
      instructions: 'Take after meals'
    });
  }

  removePrescriptionRow(index: number): void {
    this.prescriptionItems.splice(index, 1);
  }

  loadPatients(): void {
    this.patientService.getPatients().subscribe({
      next: (data) => {
        this.patientList = data;
        this.syncSelectedPatient();
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
      if (this.selectedAppointment) {
        this.syncSelectedPatient();
        if (!this.filterPatientId) {
          this.filterPatientId = this.selectedAppointment.patientId;
          this.loadConsultationsForPatient(this.filterPatientId);
        }
      }
    } else {
      this.selectedAppointment = undefined;
      this.selectedPatient = undefined;
    }
  }

  syncSelectedPatient(): void {
    if (this.selectedAppointment && this.patientList.length > 0) {
      this.selectedPatient = this.patientList.find(p => p.id === this.selectedAppointment?.patientId);
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

    const validPrescriptions = this.prescriptionItems.filter(p => p.medicineName && p.medicineName.trim() !== '');

    const req: ConsultationRequest = {
      ...this.consultationForm.value,
      prescriptionItems: validPrescriptions
    };

    this.consultationService.createConsultation(req).subscribe({
      next: (res) => {
        this.recentAppointmentId = res.appointmentId;
        this.successMessage = `Consultation summary and E-Prescription recorded successfully for ${res.patientName}! OPD Bill has been automatically generated.`;
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

  goToBilling(appointmentId?: number): void {
    if (appointmentId) {
      this.router.navigate(['/billing'], { queryParams: { appointmentId } });
    } else {
      this.router.navigate(['/billing']);
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.consultationForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }
}

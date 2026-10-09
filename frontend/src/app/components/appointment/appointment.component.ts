import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AppointmentService } from '../../services/appointment.service';
import { PatientService } from '../../services/patient.service';
import { DoctorService } from '../../services/doctor.service';
import { AppointmentResponse, AppointmentRequest } from '../../models/appointment.model';
import { Patient } from '../../models/patient.model';
import { Doctor, Department } from '../../models/doctor.model';

@Component({
  selector: 'app-appointment',
  templateUrl: './appointment.component.html',
  styleUrls: ['./appointment.component.css']
})
export class AppointmentComponent implements OnInit {
  appointments: AppointmentResponse[] = [];
  todayAppointments: AppointmentResponse[] = [];
  displayedAppointments: AppointmentResponse[] = [];
  patientList: Patient[] = [];
  doctorsList: Doctor[] = [];
  departmentsList: Department[] = [];

  bookingForm!: FormGroup;
  viewMode: 'today' | 'all' = 'today';
  isLoading: boolean = false;
  isSubmitting: boolean = false;
  hasSlotConflict: boolean = false;
  conflictWarningMessage: string = '';
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadPatients();
    this.loadDoctors();
    this.loadAppointments();

    this.route.queryParams.subscribe(params => {
      if (params['patientId']) {
        this.bookingForm.patchValue({
          patientId: Number(params['patientId'])
        });
      }
    });

    // Watch for doctor and time changes to check for slot conflicts in real-time
    this.bookingForm.valueChanges.subscribe(() => {
      this.checkConflict();
    });
  }

  initForm(): void {
    const now = new Date();
    now.setHours(now.getHours() + 1, 0, 0, 0);
    const localIso = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

    this.bookingForm = this.fb.group({
      patientId: [null, [Validators.required]],
      doctorName: ['', [Validators.required]],
      appointmentDateTime: [localIso, [Validators.required]]
    });
    this.hasSlotConflict = false;
    this.conflictWarningMessage = '';
  }

  loadPatients(): void {
    this.patientService.getPatients().subscribe({
      next: (data) => {
        this.patientList = data;
      }
    });
  }

  loadDoctors(): void {
    this.doctorService.getDoctors().subscribe({
      next: (docs) => {
        this.doctorsList = docs;
      }
    });
    this.doctorService.getDepartments().subscribe({
      next: (depts) => {
        this.departmentsList = depts;
      }
    });
  }

  checkConflict(): void {
    const doctor = this.bookingForm.get('doctorName')?.value;
    const time = this.bookingForm.get('appointmentDateTime')?.value;

    if (!doctor || !time) {
      this.hasSlotConflict = false;
      this.conflictWarningMessage = '';
      return;
    }

    this.doctorService.checkSlotConflict(doctor, time).subscribe({
      next: (res) => {
        this.hasSlotConflict = res.conflict;
        if (res.conflict) {
          this.conflictWarningMessage = `⚠️ Doctor "${doctor}" already has a scheduled appointment within 30 minutes of this time.`;
        } else {
          this.conflictWarningMessage = '';
        }
      },
      error: () => {
        this.hasSlotConflict = false;
      }
    });
  }

  loadAppointments(): void {
    this.isLoading = true;
    this.appointmentService.getAllAppointments().subscribe({
      next: (all) => {
        this.appointments = all;
        this.appointmentService.getTodayAppointments().subscribe({
          next: (today) => {
            this.todayAppointments = today;
            this.updateDisplayed();
            this.isLoading = false;
          },
          error: () => {
            this.updateDisplayed();
            this.isLoading = false;
          }
        });
      },
      error: () => {
        this.errorMessage = 'Failed to load appointments.';
        this.isLoading = false;
      }
    });
  }

  get todayCount(): number {
    return this.todayAppointments.length;
  }

  setViewMode(mode: 'today' | 'all'): void {
    this.viewMode = mode;
    this.updateDisplayed();
  }

  updateDisplayed(): void {
    if (this.viewMode === 'today') {
      this.displayedAppointments = this.todayAppointments;
    } else {
      this.displayedAppointments = this.appointments;
    }
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      return;
    }

    if (this.hasSlotConflict) {
      this.errorMessage = 'Cannot schedule appointment due to a slot conflict with the chosen doctor.';
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const req: AppointmentRequest = this.bookingForm.value;

    this.appointmentService.bookAppointment(req).subscribe({
      next: (res) => {
        this.successMessage = `Appointment #${res.id} booked successfully for ${res.patientName} with ${res.doctorName}!`;
        this.isSubmitting = false;
        this.initForm();
        this.loadAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to book appointment.';
        this.isSubmitting = false;
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.bookingForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  startConsultation(appt: AppointmentResponse): void {
    this.router.navigate(['/consultations'], {
      queryParams: {
        appointmentId: appt.id,
        patientId: appt.patientId
      }
    });
  }
}

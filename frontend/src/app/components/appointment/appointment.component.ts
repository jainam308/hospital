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

  selectedDoctor?: Doctor;
  minDateTime: string = '';

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
    this.updateMinDateTime();
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

  updateMinDateTime(): void {
    const now = new Date();
    // Offset for local ISO string
    this.minDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  }

  initForm(): void {
    this.updateMinDateTime();
    const now = new Date();
    // Default to tomorrow 10:00 AM or next hour
    now.setHours(now.getHours() + 1, 0, 0, 0);
    if (now.getHours() < 9) now.setHours(9, 0, 0, 0);
    if (now.getHours() >= 18) {
      now.setDate(now.getDate() + 1);
      now.setHours(10, 0, 0, 0);
    }
    const localIso = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

    this.bookingForm = this.fb.group({
      patientId: [null, [Validators.required]],
      doctorName: ['', [Validators.required]],
      appointmentDateTime: [localIso, [Validators.required]]
    });
    this.hasSlotConflict = false;
    this.conflictWarningMessage = '';
    this.selectedDoctor = undefined;
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
    const doctorString = this.bookingForm.get('doctorName')?.value;
    const timeString = this.bookingForm.get('appointmentDateTime')?.value;

    if (!doctorString || !timeString) {
      this.hasSlotConflict = false;
      this.conflictWarningMessage = '';
      this.selectedDoctor = undefined;
      return;
    }

    // Match selected doctor object
    this.selectedDoctor = this.doctorsList.find(d =>
      doctorString.includes(d.name)
    );

    const chosenDate = new Date(timeString);
    const now = new Date();

    // 1. Business Logic: Past date check
    if (chosenDate.getTime() < now.getTime() - 60000) {
      this.hasSlotConflict = true;
      this.conflictWarningMessage = '⚠️ Selected appointment time cannot be in the past.';
      return;
    }

    const hour = chosenDate.getHours();
    const minute = chosenDate.getMinutes();

    // 2. Business Logic: Operating hours check
    if (hour < 9 || (hour === 18 && minute > 0) || hour > 18) {
      this.hasSlotConflict = true;
      this.conflictWarningMessage = `⚠️ OPD Operating Hours are 09:00 to 18:00. Time ${chosenDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} is outside clinic hours.`;
      return;
    }

    // 3. Business Logic: Lunch break check
    if (hour === 13) {
      this.hasSlotConflict = true;
      this.conflictWarningMessage = '⚠️ Doctor Lunch Break (13:00 - 14:00). Please select Morning Batch (09:00 - 13:00) or Evening Batch (14:00 - 18:00).';
      return;
    }

    // 4. Business Logic: Doctor shift check
    if (this.selectedDoctor?.shift === 'MORNING' && hour >= 14) {
      this.hasSlotConflict = true;
      this.conflictWarningMessage = `⚠️ ${this.selectedDoctor.name} is only available in the Morning Batch (09:00 - 13:00).`;
      return;
    }
    if (this.selectedDoctor?.shift === 'EVENING' && hour < 13) {
      this.hasSlotConflict = true;
      this.conflictWarningMessage = `⚠️ ${this.selectedDoctor.name} is only available in the Evening Batch (14:00 - 18:00).`;
      return;
    }

    // 5. Server-side 30-minute slot conflict check
    this.doctorService.checkSlotConflict(doctorString, timeString).subscribe({
      next: (res) => {
        this.hasSlotConflict = res.conflict;
        if (res.conflict) {
          this.conflictWarningMessage = `⚠️ Doctor already has a scheduled 30-minute appointment slot near this time. Please pick another slot.`;
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
      this.errorMessage = this.conflictWarningMessage || 'Cannot schedule appointment due to validation rules or slot conflict.';
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

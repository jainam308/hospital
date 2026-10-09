package com.hospital.opd.config;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Gender;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.AppointmentService;
import com.hospital.opd.service.ConsultationService;
import com.hospital.opd.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;

    public DataInitializer(PatientRepository patientRepository,
                           PatientService patientService,
                           AppointmentService appointmentService,
                           ConsultationService consultationService) {
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.consultationService = consultationService;
    }

    @Override
    public void run(String... args) {
        if (patientRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("Seeding initial demo data for OPD Mini-Module...");

        // 1. Seed Patients
        PatientDTO p1 = patientService.createPatient(new PatientDTO(null, "Rahul Verma", Gender.MALE, 34, "9876543210", null));
        PatientDTO p2 = patientService.createPatient(new PatientDTO(null, "Priya Sharma", Gender.FEMALE, 28, "9823456789", null));
        PatientDTO p3 = patientService.createPatient(new PatientDTO(null, "Amit Patel", Gender.MALE, 52, "9123456780", null));
        PatientDTO p4 = patientService.createPatient(new PatientDTO(null, "Sunita Rao", Gender.FEMALE, 45, "9988776655", null));

        // 2. Seed Appointments for Today
        LocalDateTime now = LocalDateTime.now();

        var appt1 = appointmentService.bookAppointment(new AppointmentRequestDTO(
                p1.getId(),
                "Dr. Rajesh Gupta (General Medicine)",
                now.withHour(10).withMinute(0).withSecond(0)
        ));

        var appt2 = appointmentService.bookAppointment(new AppointmentRequestDTO(
                p2.getId(),
                "Dr. Anjali Menon (Cardiology)",
                now.withHour(11).withMinute(30).withSecond(0)
        ));

        var appt3 = appointmentService.bookAppointment(new AppointmentRequestDTO(
                p3.getId(),
                "Dr. Rajesh Gupta (General Medicine)",
                now.withHour(14).withMinute(0).withSecond(0)
        ));

        // 3. Seed an already completed consultation for p1
        consultationService.createConsultation(new ConsultationRequestDTO(
                appt1.getId(),
                "120/80 mmHg",
                72,
                98.4,
                "Patient reported mild fever and throat irritation for 2 days. Prescribed Paracetamol and warm saline gargles. Advised 3 days rest."
        ));

        log.info("Initial demo data seeded successfully with 4 patients and appointments.");
    }
}

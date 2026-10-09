package com.hospital.opd.config;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.dto.PrescriptionItemDTO;
import com.hospital.opd.entity.Department;
import com.hospital.opd.entity.Doctor;
import com.hospital.opd.entity.Gender;
import com.hospital.opd.repository.DepartmentRepository;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.AppointmentService;
import com.hospital.opd.service.ConsultationService;
import com.hospital.opd.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;

    public DataInitializer(PatientRepository patientRepository,
                           DepartmentRepository departmentRepository,
                           DoctorRepository doctorRepository,
                           PatientService patientService,
                           AppointmentService appointmentService,
                           ConsultationService consultationService) {
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
        this.consultationService = consultationService;
    }

    @Override
    public void run(String... args) {
        // Seed departments and doctors if not present
        if (departmentRepository.count() == 0) {
            log.info("Seeding departments and doctors...");
            Department generalMed = departmentRepository.save(new Department(null, "General Medicine", "GENMED", "General adult health and acute primary care"));
            Department cardiology = departmentRepository.save(new Department(null, "Cardiology", "CARDIO", "Heart diseases and cardiovascular healthcare"));
            Department orthopedics = departmentRepository.save(new Department(null, "Orthopedics", "ORTHO", "Bone, joint, and musculoskeletal disorders"));
            Department pediatrics = departmentRepository.save(new Department(null, "Pediatrics", "PEDIATRIC", "Infant, child, and adolescent healthcare"));

            doctorRepository.save(new Doctor(null, "Dr. Rajesh Gupta", generalMed, "Senior Consultant Physician", BigDecimal.valueOf(500.00), "Room 101"));
            doctorRepository.save(new Doctor(null, "Dr. Anjali Menon", cardiology, "Interventional Cardiologist", BigDecimal.valueOf(800.00), "Room 204"));
            doctorRepository.save(new Doctor(null, "Dr. Vikram Sethi", orthopedics, "Orthopedic Surgeon", BigDecimal.valueOf(700.00), "Room 305"));
            doctorRepository.save(new Doctor(null, "Dr. Sneha Kulkarni", pediatrics, "Pediatric Specialist", BigDecimal.valueOf(600.00), "Room 108"));
        }

        if (patientRepository.count() > 0) {
            log.info("Database already contains patients. Skipping initial patient seeding.");
            return;
        }

        log.info("Seeding initial demo data for OPD Mini-Module (v2.0)...");

        // 1. Seed Patients with Medical Profiles
        PatientDTO p1 = patientService.createPatient(new PatientDTO(null, "Rahul Verma", Gender.MALE, 34, "9876543210",
                "B+", "Penicillin, Amoxicillin", "Asthma", null));
        PatientDTO p2 = patientService.createPatient(new PatientDTO(null, "Priya Sharma", Gender.FEMALE, 28, "9823456789",
                "O+", "None", "None", null));
        PatientDTO p3 = patientService.createPatient(new PatientDTO(null, "Amit Patel", Gender.MALE, 52, "9123456780",
                "A+", "Sulfa drugs", "Type 2 Diabetes, Hypertension", null));
        PatientDTO p4 = patientService.createPatient(new PatientDTO(null, "Sunita Rao", Gender.FEMALE, 45, "9988776655",
                "AB+", "Aspirin, Ibuprofen", "Hypothyroidism", null));

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
                "Dr. Vikram Sethi (Orthopedics)",
                now.withHour(14).withMinute(0).withSecond(0)
        ));

        // 3. Seed an already completed consultation with E-Prescription for p1
        List<PrescriptionItemDTO> rxItems = List.of(
                new PrescriptionItemDTO(null, "Paracetamol 650mg", "650mg", "1-0-1 (Twice daily)", "3 days", "Take after food for fever control"),
                new PrescriptionItemDTO(null, "Cetirizine 10mg", "10mg", "0-0-1 (Nightly)", "5 days", "For allergy relief and runny nose"),
                new PrescriptionItemDTO(null, "Salbutamol Inhaler", "100mcg", "2 puffs PRN", "14 days", "Use only during acute wheezing / shortness of breath")
        );

        consultationService.createConsultation(new ConsultationRequestDTO(
                appt1.getId(),
                "120/80 mmHg",
                72,
                98.4,
                "Patient presented with mild fever and allergic rhinitis. History of asthma noted. Advised warm hydration and prescribed supportive medications.",
                rxItems
        ));

        log.info("Initial demo data seeded successfully with patients, appointments, prescriptions, and auto-generated bills.");
    }
}

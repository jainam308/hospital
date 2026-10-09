package com.hospital.opd.service.impl;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO requestDTO) {
        // 1. Business Logic: Patient existence check
        Patient patient = patientRepository.findById(requestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot book appointment. Patient not found with ID: " + requestDTO.getPatientId()));

        LocalDateTime reqTime = requestDTO.getAppointmentDateTime();

        // 2. Business Logic: Past date/time prevention (allowing 2 mins leeway for clock skew)
        if (reqTime.isBefore(LocalDateTime.now().minusMinutes(2))) {
            throw new BadRequestException("Cannot book an appointment in the past. Requested time: " + reqTime);
        }

        // 3. Business Logic: Clinic Operating Hours & Batches
        int hour = reqTime.getHour();
        int minute = reqTime.getMinute();
        if (hour < 9 || (hour == 18 && minute > 0) || hour > 18) {
            throw new BadRequestException("OPD Clinic operates from 09:00 to 18:00. The requested time (" + reqTime.toLocalTime() + ") is outside operational clinic hours.");
        }

        // Lunch break between 13:00 and 14:00
        if (hour == 13) {
            throw new BadRequestException("Requested time (" + reqTime.toLocalTime() + ") falls during the doctor lunch break (13:00 - 14:00). Please select Morning Batch (09:00 - 13:00) or Evening Batch (14:00 - 18:00).");
        }

        // 4. Business Logic: Extract base doctor name & check shift / quota
        String fullDoctorString = requestDTO.getDoctorName().trim();
        String baseDoctorName = fullDoctorString;
        if (baseDoctorName.contains(" (")) {
            baseDoctorName = baseDoctorName.substring(0, baseDoctorName.indexOf(" (")).trim();
        }

        Optional<Doctor> doctorOpt = doctorRepository.findByNameIgnoreCase(baseDoctorName);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            if (!doctor.isActive()) {
                throw new BadRequestException("Doctor " + doctor.getName() + " is currently inactive.");
            }

            // Shift validation
            if (doctor.getShift() == DoctorShift.MORNING && hour >= 14) {
                throw new BadRequestException("Doctor " + doctor.getName() + " only consults in the Morning Batch (09:00 - 13:00).");
            }
            if (doctor.getShift() == DoctorShift.EVENING && hour < 13) {
                throw new BadRequestException("Doctor " + doctor.getName() + " only consults in the Evening Batch (14:00 - 18:00).");
            }

            // Quota limit per day
            LocalDateTime startOfDay = reqTime.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = reqTime.toLocalDate().atTime(LocalTime.MAX);
            long bookedCount = appointmentRepository.countByDoctorNameIgnoreCaseAndAppointmentDateTimeBetweenAndStatusNot(
                    fullDoctorString, startOfDay, endOfDay, AppointmentStatus.CANCELLED
            );
            int quota = (doctor.getMaxDailyQuota() != null && doctor.getMaxDailyQuota() > 0) ? doctor.getMaxDailyQuota() : 20;
            if (bookedCount >= quota) {
                String docName = doctor.getName().startsWith("Dr.") ? doctor.getName() : "Dr. " + doctor.getName();
                throw new BadRequestException(docName + " has reached the maximum appointment quota (" + quota + " patients) for " + reqTime.toLocalDate() + ". Please choose another date or doctor.");
            }
        }

        // 5. Business Logic: Overlap Prevention (Doctor & Patient - 30 minutes duration)
        LocalDateTime windowStart = reqTime.minusMinutes(29);
        LocalDateTime windowEnd = reqTime.plusMinutes(29);
        List<Appointment> overlappingAppts = appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(windowStart, windowEnd);

        for (Appointment existing : overlappingAppts) {
            if (existing.getStatus() != AppointmentStatus.CANCELLED) {
                // Doctor overlap
                if (existing.getDoctorName().equalsIgnoreCase(fullDoctorString) ||
                    (existing.getDoctorName().contains(baseDoctorName) && baseDoctorName.length() > 3)) {
                    throw new BadRequestException("Doctor Slot Conflict: " + fullDoctorString + " already has a scheduled appointment at " + existing.getAppointmentDateTime() + ". Consultation slots are 30 minutes each and cannot overlap.");
                }
                // Patient overlap
                if (existing.getPatient().getId().equals(patient.getId())) {
                    throw new BadRequestException("Patient Schedule Conflict: Patient " + patient.getName() + " already has a scheduled appointment at " + existing.getAppointmentDateTime() + " with " + existing.getDoctorName() + ". Overlapping bookings are not permitted.");
                }
            }
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctorName(fullDoctorString);
        appointment.setAppointmentDateTime(reqTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getTodayAppointments() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(startOfDay, endOfDay)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getAppointmentDateTime().compareTo(b.getAppointmentDateTime()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDTO updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToDTO(updated);
    }

    private AppointmentResponseDTO mapToDTO(Appointment appt) {
        return new AppointmentResponseDTO(
                appt.getId(),
                appt.getPatient().getId(),
                appt.getPatient().getName(),
                appt.getPatient().getPhoneNumber(),
                appt.getDoctorName(),
                appt.getAppointmentDateTime(),
                appt.getStatus(),
                appt.getCreatedAt()
        );
    }
}

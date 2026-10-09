package com.hospital.opd.service.impl;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Patient;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO requestDTO) {
        Patient patient = patientRepository.findById(requestDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot book appointment. Patient not found with ID: " + requestDTO.getPatientId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctorName(requestDTO.getDoctorName().trim());
        appointment.setAppointmentDateTime(requestDTO.getAppointmentDateTime());
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

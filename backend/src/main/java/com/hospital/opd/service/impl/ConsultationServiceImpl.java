package com.hospital.opd.service.impl;

import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Consultation;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.ConsultationRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.ConsultationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                   AppointmentRepository appointmentRepository,
                                   PatientRepository patientRepository) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO) {
        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + requestDTO.getAppointmentId()));

        if (consultationRepository.existsByAppointmentId(requestDTO.getAppointmentId())) {
            throw new BadRequestException("Consultation has already been recorded for this appointment");
        }

        Consultation consultation = new Consultation();
        consultation.setAppointment(appointment);
        consultation.setPatient(appointment.getPatient());
        consultation.setBloodPressure(requestDTO.getBloodPressure().trim());
        consultation.setHeartRate(requestDTO.getHeartRate());
        consultation.setTemperature(requestDTO.getTemperature());
        consultation.setNotes(requestDTO.getNotes().trim());
        consultation.setConsultationDate(LocalDateTime.now());

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        Consultation saved = consultationRepository.save(consultation);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getConsultationsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultationByAppointment(Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment ID: " + appointmentId));
        return mapToDTO(consultation);
    }

    private ConsultationResponseDTO mapToDTO(Consultation c) {
        return new ConsultationResponseDTO(
                c.getId(),
                c.getAppointment().getId(),
                c.getPatient().getId(),
                c.getPatient().getName(),
                c.getAppointment().getDoctorName(),
                c.getBloodPressure(),
                c.getHeartRate(),
                c.getTemperature(),
                c.getNotes(),
                c.getConsultationDate()
        );
    }
}

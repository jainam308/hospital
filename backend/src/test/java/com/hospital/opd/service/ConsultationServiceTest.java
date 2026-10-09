package com.hospital.opd.service;

import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.ConsultationRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.impl.ConsultationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private Patient mockPatient;
    private Appointment mockAppointment;
    private Consultation mockConsultation;

    @BeforeEach
    void setUp() {
        mockPatient = new Patient(1L, "Rohan Sharma", Gender.MALE, 30, "9876543210");
        mockAppointment = new Appointment(10L, mockPatient, "Dr. Rajesh Gupta", LocalDateTime.now(), AppointmentStatus.SCHEDULED);
        mockConsultation = new Consultation(100L, mockAppointment, mockPatient, "120/80 mmHg", 72, 98.6, "Normal checkup");
        mockConsultation.setConsultationDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully record consultation, vitals, and mark appointment COMPLETED")
    void testCreateConsultation_Success() {
        ConsultationRequestDTO request = new ConsultationRequestDTO(10L, "120/80 mmHg", 72, 98.6, "Normal checkup");

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));
        when(consultationRepository.existsByAppointmentId(10L)).thenReturn(false);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(mockConsultation);

        ConsultationResponseDTO response = consultationService.createConsultation(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("120/80 mmHg", response.getBloodPressure());
        assertEquals(72, response.getHeartRate());
        assertEquals(98.6, response.getTemperature());
        assertEquals("Normal checkup", response.getNotes());
        assertEquals(AppointmentStatus.COMPLETED, mockAppointment.getStatus());

        verify(appointmentRepository, times(1)).save(mockAppointment);
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if consultation already exists for the appointment")
    void testCreateConsultation_Duplicate_ThrowsBadRequest() {
        ConsultationRequestDTO request = new ConsultationRequestDTO(10L, "120/80 mmHg", 72, 98.6, "Normal checkup");

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));
        when(consultationRepository.existsByAppointmentId(10L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> consultationService.createConsultation(request));
        verify(consultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve consultations for a valid patient")
    void testGetConsultationsByPatient_Success() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L))
                .thenReturn(List.of(mockConsultation));

        List<ConsultationResponseDTO> result = consultationService.getConsultationsByPatient(1L);

        assertEquals(1, result.size());
        assertEquals("Rohan Sharma", result.get(0).getPatientName());
        assertEquals("120/80 mmHg", result.get(0).getBloodPressure());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if patient does not exist when viewing consultations")
    void testGetConsultationsByPatient_NotFound() {
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> consultationService.getConsultationsByPatient(999L));
    }
}

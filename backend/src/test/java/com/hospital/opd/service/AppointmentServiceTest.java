package com.hospital.opd.service;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Gender;
import com.hospital.opd.entity.Patient;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.impl.AppointmentServiceImpl;
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
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient mockPatient;
    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        mockPatient = new Patient(1L, "Rohan Sharma", Gender.MALE, 30, "9876543210");
        mockAppointment = new Appointment(10L, mockPatient, "Dr. Rajesh Gupta", LocalDateTime.now().plusHours(2), AppointmentStatus.SCHEDULED);
        mockAppointment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully book an appointment for an existing patient")
    void testBookAppointment_Success() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", LocalDateTime.now().plusHours(2));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        AppointmentResponseDTO response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Rohan Sharma", response.getPatientName());
        assertEquals("Dr. Rajesh Gupta", response.getDoctorName());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when booking appointment for non-existent patient")
    void testBookAppointment_PatientNotFound() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(999L, "Dr. Rajesh Gupta", LocalDateTime.now().plusHours(2));

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.bookAppointment(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve today's appointments ordered by time")
    void testGetTodayAppointments_Success() {
        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of(mockAppointment));

        List<AppointmentResponseDTO> result = appointmentService.getTodayAppointments();

        assertEquals(1, result.size());
        assertEquals("Dr. Rajesh Gupta", result.get(0).getDoctorName());
    }

    @Test
    @DisplayName("Should update appointment status")
    void testUpdateStatus_Success() {
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        AppointmentResponseDTO result = appointmentService.updateStatus(10L, AppointmentStatus.COMPLETED);

        assertNotNull(result);
        verify(appointmentRepository, times(1)).save(mockAppointment);
    }
}

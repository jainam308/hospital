package com.hospital.opd.service;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient mockPatient;
    private Appointment mockAppointment;
    private Doctor mockDoctor;
    private LocalDateTime validSlot;

    @BeforeEach
    void setUp() {
        mockPatient = new Patient(1L, "Rohan Sharma", Gender.MALE, 30, "9876543210");
        validSlot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);

        Department dept = new Department(1L, "General Medicine", "GENMED", "General");
        mockDoctor = new Doctor(1L, "Dr. Rajesh Gupta", dept, "Senior Physician", BigDecimal.valueOf(500.00), "Room 101", DoctorShift.MORNING, 20, 30);

        mockAppointment = new Appointment(10L, mockPatient, "Dr. Rajesh Gupta", validSlot, AppointmentStatus.SCHEDULED);
        mockAppointment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully book an appointment for an existing patient")
    void testBookAppointment_Success() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", validSlot);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findByNameIgnoreCase("Dr. Rajesh Gupta")).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.countByDoctorNameIgnoreCaseAndAppointmentDateTimeBetweenAndStatusNot(any(), any(), any(), any()))
                .thenReturn(0L);
        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of());
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
    @DisplayName("Should reject appointment booking in the past")
    void testBookAppointment_PastDate_ThrowsBadRequest() {
        LocalDateTime past = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", past);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(request));
        assertTrue(ex.getMessage().contains("Cannot book an appointment in the past"));
    }

    @Test
    @DisplayName("Should reject appointment during lunch break (13:00 - 14:00)")
    void testBookAppointment_LunchBreak_ThrowsBadRequest() {
        LocalDateTime lunchTime = LocalDateTime.now().plusDays(1).withHour(13).withMinute(15);
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", lunchTime);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(request));
        assertTrue(ex.getMessage().contains("doctor lunch break"));
    }

    @Test
    @DisplayName("Should reject appointment when doctor has an overlapping slot within 30 minutes")
    void testBookAppointment_DoctorSlotConflict_ThrowsBadRequest() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", validSlot);

        Appointment conflictAppt = new Appointment(99L, new Patient(2L, "Other", Gender.MALE, 40, "9111111111"), "Dr. Rajesh Gupta", validSlot.plusMinutes(10), AppointmentStatus.SCHEDULED);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findByNameIgnoreCase("Dr. Rajesh Gupta")).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of(conflictAppt));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(request));
        assertTrue(ex.getMessage().contains("Doctor Slot Conflict"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject appointment when patient has an overlapping slot with another doctor")
    void testBookAppointment_PatientConflict_ThrowsBadRequest() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", validSlot);

        Appointment conflictAppt = new Appointment(99L, mockPatient, "Dr. Anjali Menon", validSlot.plusMinutes(15), AppointmentStatus.SCHEDULED);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findByNameIgnoreCase("Dr. Rajesh Gupta")).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of(conflictAppt));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(request));
        assertTrue(ex.getMessage().contains("Patient Schedule Conflict"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject appointment when doctor quota is exceeded")
    void testBookAppointment_QuotaExceeded_ThrowsBadRequest() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", validSlot);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));
        when(doctorRepository.findByNameIgnoreCase("Dr. Rajesh Gupta")).thenReturn(Optional.of(mockDoctor));
        when(appointmentRepository.countByDoctorNameIgnoreCaseAndAppointmentDateTimeBetweenAndStatusNot(any(), any(), any(), any()))
                .thenReturn(20L); // Max quota reached

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.bookAppointment(request));
        assertTrue(ex.getMessage().contains("maximum appointment quota"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when booking appointment for non-existent patient")
    void testBookAppointment_PatientNotFound() {
        AppointmentRequestDTO request = new AppointmentRequestDTO(999L, "Dr. Rajesh Gupta", validSlot);

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

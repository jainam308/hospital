package com.hospital.opd.service;

import com.hospital.opd.dto.DepartmentDTO;
import com.hospital.opd.dto.DoctorDTO;
import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Department;
import com.hospital.opd.entity.Doctor;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.DepartmentRepository;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.service.impl.DoctorServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Department dept;
    private Doctor doc;

    @BeforeEach
    void setUp() {
        dept = new Department(1L, "Cardiology", "CARDIO", "Heart healthcare");
        doc = new Doctor(10L, "Dr. Anjali Menon", dept, "Interventional Cardiologist", BigDecimal.valueOf(800.00), "Room 204");
    }

    @Test
    @DisplayName("Should return all active doctors")
    void testGetAllDoctors() {
        when(doctorRepository.findByActiveTrue()).thenReturn(List.of(doc));

        List<DoctorDTO> result = doctorService.getAllDoctors();
        assertEquals(1, result.size());
        assertEquals("Dr. Anjali Menon", result.get(0).getName());
        assertEquals("Cardiology", result.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("Should return doctor by ID")
    void testGetDoctorById() {
        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doc));

        DoctorDTO result = doctorService.getDoctorById(10L);
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(800.00), result.getConsultationFee());
    }

    @Test
    @DisplayName("Should detect slot conflict when doctor already has an appointment within 30 mins")
    void testHasSlotConflict_True() {
        LocalDateTime requested = LocalDateTime.now().plusHours(2);
        Appointment conflictAppt = new Appointment();
        conflictAppt.setDoctorName("Dr. Anjali Menon");
        conflictAppt.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of(conflictAppt));

        boolean conflict = doctorService.hasSlotConflict("Dr. Anjali Menon", requested);
        assertTrue(conflict);
    }

    @Test
    @DisplayName("Should detect no conflict when doctor is free")
    void testHasSlotConflict_False() {
        LocalDateTime requested = LocalDateTime.now().plusHours(2);

        when(appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(any(), any()))
                .thenReturn(List.of());

        boolean conflict = doctorService.hasSlotConflict("Dr. Anjali Menon", requested);
        assertFalse(conflict);
    }

    @Test
    @DisplayName("Should return all departments")
    void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(dept));

        List<DepartmentDTO> result = doctorService.getAllDepartments();
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getName());
    }
}

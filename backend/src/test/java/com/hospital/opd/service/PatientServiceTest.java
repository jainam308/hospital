package com.hospital.opd.service;

import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.entity.Gender;
import com.hospital.opd.entity.Patient;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient mockPatient;

    @BeforeEach
    void setUp() {
        mockPatient = new Patient(1L, "Rohan Sharma", Gender.MALE, 30, "9876543210");
        mockPatient.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully create and persist a new patient")
    void testCreatePatient_Success() {
        PatientDTO inputDto = new PatientDTO(null, "Rohan Sharma", Gender.MALE, 30, "9876543210", null);

        when(patientRepository.findByPhoneNumber("9876543210")).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(mockPatient);

        PatientDTO result = patientService.createPatient(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Rohan Sharma", result.getName());
        assertEquals(Gender.MALE, result.getGender());
        assertEquals(30, result.getAge());
        assertEquals("9876543210", result.getPhoneNumber());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should reject patient creation if phone number already exists")
    void testCreatePatient_DuplicatePhone_ThrowsBadRequest() {
        PatientDTO inputDto = new PatientDTO(null, "Duplicate Phone User", Gender.MALE, 25, "9876543210", null);

        when(patientRepository.findByPhoneNumber("9876543210")).thenReturn(Optional.of(mockPatient));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> patientService.createPatient(inputDto));
        assertTrue(ex.getMessage().contains("already registered"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject patient creation if email already exists")
    void testCreatePatient_DuplicateEmail_ThrowsBadRequest() {
        PatientDTO inputDto = new PatientDTO(null, "User A", Gender.FEMALE, 29, "9999988888", "rohan@example.com", "A+", "None", "None", null);

        when(patientRepository.findByPhoneNumber("9999988888")).thenReturn(Optional.empty());
        when(patientRepository.findByEmailIgnoreCase("rohan@example.com")).thenReturn(Optional.of(mockPatient));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> patientService.createPatient(inputDto));
        assertTrue(ex.getMessage().contains("already registered"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject patient creation if blood group is invalid")
    void testCreatePatient_InvalidBloodGroup_ThrowsBadRequest() {
        PatientDTO inputDto = new PatientDTO(null, "User B", Gender.MALE, 35, "9111122222", "XYZ", null, null, null);

        when(patientRepository.findByPhoneNumber("9111122222")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> patientService.createPatient(inputDto));
        assertTrue(ex.getMessage().contains("Invalid blood group"));
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve all patients ordered by created date")
    void testGetAllPatients_Success() {
        Patient p2 = new Patient(2L, "Ananya Gupta", Gender.FEMALE, 25, "9123456789");
        p2.setCreatedAt(LocalDateTime.now());

        when(patientRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(mockPatient, p2));

        List<PatientDTO> list = patientService.getAllPatients();

        assertEquals(2, list.size());
        assertEquals("Rohan Sharma", list.get(0).getName());
        assertEquals("Ananya Gupta", list.get(1).getName());
        verify(patientRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should search patients by query string")
    void testSearchPatients_WithQuery() {
        when(patientRepository.searchByNameOrPhone("Rohan")).thenReturn(List.of(mockPatient));

        List<PatientDTO> list = patientService.searchPatients("Rohan");

        assertEquals(1, list.size());
        assertEquals("Rohan Sharma", list.get(0).getName());
        verify(patientRepository, times(1)).searchByNameOrPhone("Rohan");
    }

    @Test
    @DisplayName("Should return all patients when search query is empty or blank")
    void testSearchPatients_EmptyQuery() {
        when(patientRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(mockPatient));

        List<PatientDTO> list = patientService.searchPatients("   ");

        assertEquals(1, list.size());
        verify(patientRepository, times(1)).findAllByOrderByCreatedAtDesc();
        verify(patientRepository, never()).searchByNameOrPhone(any());
    }

    @Test
    @DisplayName("Should find patient by ID successfully")
    void testGetPatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(mockPatient));

        PatientDTO result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Rohan Sharma", result.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient ID does not exist")
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(999L));
    }
}

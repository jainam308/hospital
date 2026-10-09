package com.hospital.opd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.entity.Gender;
import com.hospital.opd.exception.GlobalExceptionHandler;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    @Test
    @DisplayName("POST /api/patients - Should create patient and return 201 Created")
    void testCreatePatient_Valid_Returns201() throws Exception {
        PatientDTO input = new PatientDTO(null, "Vikram Malhotra", Gender.MALE, 42, "9876501234", null);
        PatientDTO created = new PatientDTO(1L, "Vikram Malhotra", Gender.MALE, 42, "9876501234", LocalDateTime.now());

        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Vikram Malhotra"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.age").value(42));
    }

    @Test
    @DisplayName("POST /api/patients - Should return 400 Bad Request when validation fails")
    void testCreatePatient_Invalid_Returns400() throws Exception {
        PatientDTO invalidInput = new PatientDTO(null, "", null, -5, "abc", null);

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("GET /api/patients - Should return list of patients with 200 OK")
    void testGetPatients_ReturnsList() throws Exception {
        PatientDTO p = new PatientDTO(1L, "Vikram Malhotra", Gender.MALE, 42, "9876501234", LocalDateTime.now());
        when(patientService.getAllPatients()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Vikram Malhotra"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - Should return 404 when patient does not exist")
    void testGetPatientById_NotFound_Returns404() throws Exception {
        when(patientService.getPatientById(999L)).thenThrow(new ResourceNotFoundException("Patient not found with ID: 999"));

        mockMvc.perform(get("/api/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Patient not found with ID: 999"));
    }
}

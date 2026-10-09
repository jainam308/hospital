package com.hospital.opd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.GlobalExceptionHandler;
import com.hospital.opd.service.ConsultationService;
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

@WebMvcTest(ConsultationController.class)
@Import(GlobalExceptionHandler.class)
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConsultationService consultationService;

    @Test
    @DisplayName("POST /api/consultations - Should record vitals and return 201 Created")
    void testCreateConsultation_Returns201() throws Exception {
        ConsultationRequestDTO request = new ConsultationRequestDTO(10L, "120/80 mmHg", 74, 98.6, "Healthy vitals");
        ConsultationResponseDTO response = new ConsultationResponseDTO(100L, 10L, 1L, "Vikram Malhotra",
                "Dr. Rajesh Gupta", "120/80 mmHg", 74, 98.6, "Healthy vitals", LocalDateTime.now());

        when(consultationService.createConsultation(any(ConsultationRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/consultations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.bloodPressure").value("120/80 mmHg"))
                .andExpect(jsonPath("$.heartRate").value(74));
    }

    @Test
    @DisplayName("POST /api/consultations - Should return 400 Bad Request when duplicate consultation is recorded")
    void testCreateConsultation_Duplicate_Returns400() throws Exception {
        ConsultationRequestDTO request = new ConsultationRequestDTO(10L, "120/80 mmHg", 74, 98.6, "Healthy vitals");

        when(consultationService.createConsultation(any(ConsultationRequestDTO.class)))
                .thenThrow(new BadRequestException("Consultation has already been recorded for this appointment"));

        mockMvc.perform(post("/api/consultations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Consultation has already been recorded for this appointment"));
    }

    @Test
    @DisplayName("GET /api/consultations/patient/{patientId} - Should return patient's consultation history")
    void testGetConsultationsByPatient_ReturnsList() throws Exception {
        ConsultationResponseDTO response = new ConsultationResponseDTO(100L, 10L, 1L, "Vikram Malhotra",
                "Dr. Rajesh Gupta", "120/80 mmHg", 74, 98.6, "Healthy vitals", LocalDateTime.now());

        when(consultationService.getConsultationsByPatient(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/consultations/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bloodPressure").value("120/80 mmHg"));
    }
}

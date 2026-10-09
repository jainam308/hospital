package com.hospital.opd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.exception.GlobalExceptionHandler;
import com.hospital.opd.service.AppointmentService;
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

@WebMvcTest(AppointmentController.class)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("POST /api/appointments - Should schedule appointment and return 201 Created")
    void testBookAppointment_Returns201() throws Exception {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        AppointmentRequestDTO request = new AppointmentRequestDTO(1L, "Dr. Rajesh Gupta", futureTime);
        AppointmentResponseDTO response = new AppointmentResponseDTO(10L, 1L, "Vikram Malhotra", "9876501234",
                "Dr. Rajesh Gupta", futureTime, AppointmentStatus.SCHEDULED, LocalDateTime.now());

        when(appointmentService.bookAppointment(any(AppointmentRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.patientName").value("Vikram Malhotra"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/appointments/today - Should return today's appointments list")
    void testGetTodayAppointments_ReturnsList() throws Exception {
        AppointmentResponseDTO response = new AppointmentResponseDTO(10L, 1L, "Vikram Malhotra", "9876501234",
                "Dr. Rajesh Gupta", LocalDateTime.now(), AppointmentStatus.SCHEDULED, LocalDateTime.now());

        when(appointmentService.getTodayAppointments()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/appointments/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Rajesh Gupta"));
    }
}

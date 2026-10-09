package com.hospital.opd.controller;

import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import com.hospital.opd.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ResponseEntity<ConsultationResponseDTO> createConsultation(@Valid @RequestBody ConsultationRequestDTO requestDTO) {
        ConsultationResponseDTO created = consultationService.createConsultation(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConsultationResponseDTO>> getConsultationsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(consultationService.getConsultationsByPatient(patientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ConsultationResponseDTO> getConsultationByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(consultationService.getConsultationByAppointment(appointmentId));
    }
}

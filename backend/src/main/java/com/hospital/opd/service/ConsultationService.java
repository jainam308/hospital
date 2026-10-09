package com.hospital.opd.service;

import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import java.util.List;

public interface ConsultationService {
    ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO);
    List<ConsultationResponseDTO> getConsultationsByPatient(Long patientId);
    ConsultationResponseDTO getConsultationByAppointment(Long appointmentId);
}

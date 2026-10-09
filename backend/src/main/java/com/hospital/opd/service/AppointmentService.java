package com.hospital.opd.service;

import com.hospital.opd.dto.AppointmentRequestDTO;
import com.hospital.opd.dto.AppointmentResponseDTO;
import com.hospital.opd.entity.AppointmentStatus;
import java.util.List;

public interface AppointmentService {
    AppointmentResponseDTO bookAppointment(AppointmentRequestDTO requestDTO);
    List<AppointmentResponseDTO> getTodayAppointments();
    List<AppointmentResponseDTO> getAllAppointments();
    List<AppointmentResponseDTO> getAppointmentsByPatient(Long patientId);
    AppointmentResponseDTO updateStatus(Long id, AppointmentStatus status);
}

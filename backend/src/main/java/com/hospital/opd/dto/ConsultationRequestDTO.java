package com.hospital.opd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ConsultationRequestDTO {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotBlank(message = "Blood pressure is required (e.g. 120/80)")
    private String bloodPressure;

    private Integer heartRate;

    private Double temperature;

    @NotBlank(message = "Consultation notes are required")
    private String notes;

    private List<PrescriptionItemDTO> prescriptionItems = new ArrayList<>();

    public ConsultationRequestDTO() {
    }

    public ConsultationRequestDTO(Long appointmentId, String bloodPressure, Integer heartRate, Double temperature, String notes) {
        this.appointmentId = appointmentId;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.notes = notes;
    }

    public ConsultationRequestDTO(Long appointmentId, String bloodPressure, Integer heartRate, Double temperature, String notes, List<PrescriptionItemDTO> prescriptionItems) {
        this.appointmentId = appointmentId;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.notes = notes;
        this.prescriptionItems = prescriptionItems != null ? prescriptionItems : new ArrayList<>();
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PrescriptionItemDTO> getPrescriptionItems() {
        return prescriptionItems;
    }

    public void setPrescriptionItems(List<PrescriptionItemDTO> prescriptionItems) {
        this.prescriptionItems = prescriptionItems;
    }
}

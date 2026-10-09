package com.hospital.opd.dto;

import jakarta.validation.constraints.NotBlank;

public class PrescriptionItemDTO {

    private Long id;

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    @NotBlank(message = "Dosage is required (e.g. 500mg, 10ml)")
    private String dosage;

    @NotBlank(message = "Frequency is required (e.g. 1-0-1)")
    private String frequency;

    @NotBlank(message = "Duration is required (e.g. 5 days)")
    private String duration;

    private String instructions;

    public PrescriptionItemDTO() {
    }

    public PrescriptionItemDTO(String medicineName, String dosage, String frequency, String duration, String instructions) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
    }

    public PrescriptionItemDTO(Long id, String medicineName, String dosage, String frequency, String duration, String instructions) {
        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}

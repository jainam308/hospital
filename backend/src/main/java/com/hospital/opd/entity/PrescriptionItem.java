package com.hospital.opd.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_id", nullable = false)
    @JsonBackReference
    private Consultation consultation;

    @Column(name = "medicine_name", nullable = false, length = 100)
    private String medicineName;

    @Column(nullable = false, length = 50)
    private String dosage; // e.g. "500 mg", "10 ml"

    @Column(nullable = false, length = 50)
    private String frequency; // e.g. "1-0-1 (Morning-Night)", "1-1-1"

    @Column(nullable = false, length = 50)
    private String duration; // e.g. "5 days", "2 weeks"

    @Column(length = 255)
    private String instructions; // e.g. "After food", "Before sleep"

    public PrescriptionItem() {
    }

    public PrescriptionItem(Long id, Consultation consultation, String medicineName, String dosage, String frequency, String duration, String instructions) {
        this.id = id;
        this.consultation = consultation;
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

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
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

package com.hospital.opd.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, length = 100)
    private String specialization;

    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee = BigDecimal.valueOf(500.00);

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false, length = 20)
    private DoctorShift shift = DoctorShift.ALL_DAY;

    @Column(name = "max_daily_quota", nullable = false)
    private Integer maxDailyQuota = 20;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes = 30;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    public Doctor() {
    }

    public Doctor(Long id, String name, Department department, String specialization, BigDecimal consultationFee, String roomNumber) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.roomNumber = roomNumber;
        this.shift = DoctorShift.ALL_DAY;
        this.maxDailyQuota = 20;
        this.slotDurationMinutes = 30;
        this.active = true;
    }

    public Doctor(Long id, String name, Department department, String specialization, BigDecimal consultationFee,
                  String roomNumber, DoctorShift shift, Integer maxDailyQuota, Integer slotDurationMinutes) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.roomNumber = roomNumber;
        this.shift = shift != null ? shift : DoctorShift.ALL_DAY;
        this.maxDailyQuota = maxDailyQuota != null ? maxDailyQuota : 20;
        this.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 30;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public DoctorShift getShift() {
        return shift;
    }

    public void setShift(DoctorShift shift) {
        this.shift = shift;
    }

    public Integer getMaxDailyQuota() {
        return maxDailyQuota;
    }

    public void setMaxDailyQuota(Integer maxDailyQuota) {
        this.maxDailyQuota = maxDailyQuota;
    }

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

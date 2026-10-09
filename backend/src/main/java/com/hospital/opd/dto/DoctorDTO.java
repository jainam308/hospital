package com.hospital.opd.dto;

import com.hospital.opd.entity.DoctorShift;
import java.math.BigDecimal;

public class DoctorDTO {

    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private BigDecimal consultationFee;
    private String roomNumber;
    private DoctorShift shift;
    private Integer maxDailyQuota;
    private Integer slotDurationMinutes;
    private String email;
    private String phone;
    private boolean active;

    public DoctorDTO() {
    }

    public DoctorDTO(Long id, String name, Long departmentId, String departmentName, String specialization, BigDecimal consultationFee, String roomNumber, String email, String phone, boolean active) {
        this(id, name, departmentId, departmentName, specialization, consultationFee, roomNumber, DoctorShift.ALL_DAY, 20, 30, email, phone, active);
    }

    public DoctorDTO(Long id, String name, Long departmentId, String departmentName, String specialization,
                     BigDecimal consultationFee, String roomNumber, DoctorShift shift, Integer maxDailyQuota,
                     Integer slotDurationMinutes, String email, String phone, boolean active) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.roomNumber = roomNumber;
        this.shift = shift != null ? shift : DoctorShift.ALL_DAY;
        this.maxDailyQuota = maxDailyQuota != null ? maxDailyQuota : 20;
        this.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 30;
        this.email = email;
        this.phone = phone;
        this.active = active;
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
        return (maxDailyQuota != null && maxDailyQuota > 0) ? maxDailyQuota : 20;
    }

    public void setMaxDailyQuota(Integer maxDailyQuota) {
        this.maxDailyQuota = maxDailyQuota;
    }

    public Integer getSlotDurationMinutes() {
        return (slotDurationMinutes != null && slotDurationMinutes > 0) ? slotDurationMinutes : 30;
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

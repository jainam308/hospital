package com.hospital.opd.dto;

import java.math.BigDecimal;

public class RazorpayOrderResponseDTO {

    private String orderId;
    private Long billId;
    private String billNumber;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private String patientName;
    private String patientPhone;
    private String companyName;

    public RazorpayOrderResponseDTO() {
    }

    public RazorpayOrderResponseDTO(String orderId, Long billId, String billNumber, BigDecimal amount, String currency, String keyId, String patientName, String patientPhone, String companyName) {
        this.orderId = orderId;
        this.billId = billId;
        this.billNumber = billNumber;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.companyName = companyName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

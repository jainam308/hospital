package com.hospital.opd.service;

import com.hospital.opd.dto.BillResponseDTO;
import com.hospital.opd.dto.PaymentVerificationDTO;
import com.hospital.opd.dto.RazorpayOrderResponseDTO;
import java.util.List;

public interface BillingService {
    BillResponseDTO generateBillForAppointment(Long appointmentId);
    RazorpayOrderResponseDTO createRazorpayOrder(Long billId);
    BillResponseDTO verifyRazorpayPayment(PaymentVerificationDTO verificationDTO);
    BillResponseDTO processCashPayment(Long billId);
    BillResponseDTO getBillByAppointment(Long appointmentId);
    List<BillResponseDTO> getBillsByPatient(Long patientId);
    List<BillResponseDTO> getAllBills();
}

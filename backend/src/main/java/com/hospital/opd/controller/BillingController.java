package com.hospital.opd.controller;

import com.hospital.opd.dto.BillResponseDTO;
import com.hospital.opd.dto.PaymentVerificationDTO;
import com.hospital.opd.dto.RazorpayOrderResponseDTO;
import com.hospital.opd.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<List<BillResponseDTO>> getAllBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<BillResponseDTO> getBillByAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(billingService.getBillByAppointment(appointmentId));
    }

    @PostMapping("/generate/{appointmentId}")
    public ResponseEntity<BillResponseDTO> generateBillForAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(billingService.generateBillForAppointment(appointmentId));
    }

    @PostMapping("/create-order/{billId}")
    public ResponseEntity<RazorpayOrderResponseDTO> createRazorpayOrder(@PathVariable Long billId) {
        return ResponseEntity.ok(billingService.createRazorpayOrder(billId));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<BillResponseDTO> verifyPayment(@Valid @RequestBody PaymentVerificationDTO verificationDTO) {
        return ResponseEntity.ok(billingService.verifyRazorpayPayment(verificationDTO));
    }

    @PostMapping("/cash-payment/{billId}")
    public ResponseEntity<BillResponseDTO> processCashPayment(@PathVariable Long billId) {
        return ResponseEntity.ok(billingService.processCashPayment(billId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BillResponseDTO>> getBillsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatient(patientId));
    }
}

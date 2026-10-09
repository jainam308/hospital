package com.hospital.opd.service.impl;

import com.hospital.opd.dto.BillResponseDTO;
import com.hospital.opd.dto.PaymentVerificationDTO;
import com.hospital.opd.dto.RazorpayOrderResponseDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.BillRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.BillingService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    private final BillRepository billRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    @Value("${razorpay.key.id:rzp_test_OPDCareDemoKey}")
    private String keyId;

    @Value("${razorpay.key.secret:SecretKeyDemoOPDCare2026}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.company.name:OPD Care Hospital}")
    private String companyName;

    @Value("${razorpay.mock.mode:true}")
    private boolean mockMode;

    public BillingServiceImpl(BillRepository billRepository,
                              AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository) {
        this.billRepository = billRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public BillResponseDTO generateBillForAppointment(Long appointmentId) {
        var existing = billRepository.findByAppointmentId(appointmentId);
        if (existing.isPresent()) {
            return mapToDTO(existing.get());
        }

        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        BigDecimal fee = BigDecimal.valueOf(500.00);
        BigDecimal tax = fee.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = fee.add(tax);

        Bill bill = new Bill();
        bill.setBillNumber("INV-" + System.currentTimeMillis() % 1000000);
        bill.setAppointment(appt);
        bill.setPatient(appt.getPatient());
        bill.setDoctorName(appt.getDoctorName());
        bill.setConsultationFee(fee);
        bill.setTaxAmount(tax);
        bill.setTotalAmount(total);
        bill.setPaymentStatus(PaymentStatus.PENDING);

        Bill saved = billRepository.save(bill);
        return mapToDTO(saved);
    }

    @Override
    public RazorpayOrderResponseDTO createRazorpayOrder(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("This bill is already settled.");
        }

        String orderId;
        int amountInPaise = bill.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue();

        if (mockMode || keyId.contains("Demo")) {
            orderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 14);
            log.info("Mock Razorpay Order generated: {}", orderId);
        } else {
            try {
                RazorpayClient client = new RazorpayClient(keyId, keySecret);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amountInPaise);
                orderRequest.put("currency", currency);
                orderRequest.put("receipt", bill.getBillNumber());

                Order order = client.orders.create(orderRequest);
                orderId = order.get("id");
            } catch (Exception e) {
                log.warn("Razorpay API call failed, falling back to simulated order ID: {}", e.getMessage());
                orderId = "order_sim_" + UUID.randomUUID().toString().substring(0, 14);
            }
        }

        bill.setRazorpayOrderId(orderId);
        billRepository.save(bill);

        return new RazorpayOrderResponseDTO(
                orderId,
                bill.getId(),
                bill.getBillNumber(),
                bill.getTotalAmount(),
                currency,
                keyId,
                bill.getPatient().getName(),
                bill.getPatient().getPhoneNumber(),
                companyName
        );
    }

    @Override
    public BillResponseDTO verifyRazorpayPayment(PaymentVerificationDTO verificationDTO) {
        Bill bill = billRepository.findById(verificationDTO.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + verificationDTO.getBillId()));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            return mapToDTO(bill);
        }

        boolean isValid = false;
        if (mockMode || keyId.contains("Demo")) {
            isValid = true;
        } else {
            try {
                JSONObject attributes = new JSONObject();
                attributes.put("razorpay_order_id", verificationDTO.getRazorpayOrderId());
                attributes.put("razorpay_payment_id", verificationDTO.getRazorpayPaymentId());
                attributes.put("razorpay_signature", verificationDTO.getRazorpaySignature());

                isValid = Utils.verifyPaymentSignature(attributes, keySecret);
            } catch (Exception e) {
                log.error("Signature verification failed: {}", e.getMessage());
                isValid = false;
            }
        }

        if (!isValid) {
            bill.setPaymentStatus(PaymentStatus.FAILED);
            billRepository.save(bill);
            throw new BadRequestException("Invalid payment signature from Razorpay. Verification failed.");
        }

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaymentMode(PaymentMode.RAZORPAY);
        bill.setRazorpayPaymentId(verificationDTO.getRazorpayPaymentId());
        bill.setRazorpaySignature(verificationDTO.getRazorpaySignature());
        bill.setPaidAt(LocalDateTime.now());

        Bill updated = billRepository.save(bill);
        return mapToDTO(updated);
    }

    @Override
    public BillResponseDTO processCashPayment(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("This bill is already settled.");
        }

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaymentMode(PaymentMode.CASH);
        bill.setPaidAt(LocalDateTime.now());

        Bill updated = billRepository.save(bill);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponseDTO getBillByAppointment(Long appointmentId) {
        Bill bill = billRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No bill found for appointment ID: " + appointmentId));
        return mapToDTO(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponseDTO> getBillsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return billRepository.findByPatientIdOrderByBilledAtDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponseDTO> getAllBills() {
        return billRepository.findAllByOrderByBilledAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BillResponseDTO mapToDTO(Bill b) {
        return new BillResponseDTO(
                b.getId(),
                b.getBillNumber(),
                b.getAppointment().getId(),
                b.getPatient().getId(),
                b.getPatient().getName(),
                b.getPatient().getPhoneNumber(),
                b.getDoctorName(),
                b.getConsultationFee(),
                b.getTaxAmount(),
                b.getTotalAmount(),
                b.getPaymentMode(),
                b.getPaymentStatus(),
                b.getRazorpayOrderId(),
                b.getRazorpayPaymentId(),
                b.getBilledAt(),
                b.getPaidAt()
        );
    }
}

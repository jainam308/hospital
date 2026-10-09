package com.hospital.opd.service;

import com.hospital.opd.dto.BillResponseDTO;
import com.hospital.opd.dto.PaymentVerificationDTO;
import com.hospital.opd.dto.RazorpayOrderResponseDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.BillRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Patient patient;
    private Appointment appointment;
    private Bill bill;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(billingService, "keyId", "rzp_test_OPDCareDemoKey");
        ReflectionTestUtils.setField(billingService, "keySecret", "SecretKeyDemoOPDCare2026");
        ReflectionTestUtils.setField(billingService, "currency", "INR");
        ReflectionTestUtils.setField(billingService, "companyName", "OPD Care Hospital");
        ReflectionTestUtils.setField(billingService, "mockMode", true);

        patient = new Patient(1L, "Rahul Verma", Gender.MALE, 34, "9876543210");
        appointment = new Appointment(10L, patient, "Dr. Rajesh Gupta", LocalDateTime.now(), AppointmentStatus.COMPLETED);
        
        bill = new Bill();
        bill.setId(100L);
        bill.setBillNumber("INV-2026-0001");
        bill.setAppointment(appointment);
        bill.setPatient(patient);
        bill.setDoctorName("Dr. Rajesh Gupta");
        bill.setConsultationFee(BigDecimal.valueOf(500.00));
        bill.setTaxAmount(BigDecimal.valueOf(90.00));
        bill.setTotalAmount(BigDecimal.valueOf(590.00));
        bill.setPaymentStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should create Razorpay order in mock mode")
    void testCreateRazorpayOrder() {
        when(billRepository.findById(100L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        RazorpayOrderResponseDTO orderResponse = billingService.createRazorpayOrder(100L);

        assertNotNull(orderResponse);
        assertEquals(100L, orderResponse.getBillId());
        assertEquals("INR", orderResponse.getCurrency());
        assertTrue(orderResponse.getOrderId().startsWith("order_mock_"));
        assertEquals(BigDecimal.valueOf(590.00), orderResponse.getAmount());
        verify(billRepository, times(1)).save(bill);
    }

    @Test
    @DisplayName("Should verify mock Razorpay payment successfully")
    void testVerifyRazorpayPayment_MockMode() {
        bill.setRazorpayOrderId("order_mock_123");
        when(billRepository.findById(100L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        PaymentVerificationDTO verification = new PaymentVerificationDTO(
                100L,
                "order_mock_123",
                "pay_mock_test_456",
                "mock_signature_opd_care"
        );

        BillResponseDTO response = billingService.verifyRazorpayPayment(verification);

        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(PaymentMode.RAZORPAY, response.getPaymentMode());
        assertEquals("pay_mock_test_456", response.getRazorpayPaymentId());
    }

    @Test
    @DisplayName("Should settle bill with Cash payment")
    void testProcessCashPayment() {
        when(billRepository.findById(100L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        BillResponseDTO response = billingService.processCashPayment(100L);

        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(PaymentMode.CASH, response.getPaymentMode());
        assertNotNull(response.getPaidAt());
    }
}

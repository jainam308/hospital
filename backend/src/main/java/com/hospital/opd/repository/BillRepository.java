package com.hospital.opd.repository;

import com.hospital.opd.entity.Bill;
import com.hospital.opd.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByAppointmentId(Long appointmentId);
    Optional<Bill> findByRazorpayOrderId(String razorpayOrderId);
    List<Bill> findByPatientIdOrderByBilledAtDesc(Long patientId);
    List<Bill> findByPaymentStatusOrderByBilledAtDesc(PaymentStatus status);
    List<Bill> findAllByOrderByBilledAtDesc();
    boolean existsByAppointmentId(Long appointmentId);
}

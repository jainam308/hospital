package com.hospital.opd.repository;

import com.hospital.opd.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    List<Consultation> findByPatientIdOrderByConsultationDateDesc(Long patientId);

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);
}

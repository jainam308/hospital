package com.hospital.opd.repository;

import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);

    List<Appointment> findByStatusOrderByAppointmentDateTimeAsc(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE " +
           "CAST(a.appointmentDateTime AS date) = CURRENT_DATE " +
           "ORDER BY a.appointmentDateTime ASC")
    List<Appointment> findTodayAppointments();

    List<Appointment> findByDoctorNameIgnoreCaseAndAppointmentDateTimeBetweenAndStatusNot(
            String doctorName, LocalDateTime start, LocalDateTime end, AppointmentStatus status);

    long countByDoctorNameIgnoreCaseAndAppointmentDateTimeBetweenAndStatusNot(
            String doctorName, LocalDateTime start, LocalDateTime end, AppointmentStatus status);
}

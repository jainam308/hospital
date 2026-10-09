package com.hospital.opd.repository;

import com.hospital.opd.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.phoneNumber LIKE CONCAT('%', :query, '%') " +
           "ORDER BY p.createdAt DESC")
    List<Patient> searchByNameOrPhone(@Param("query") String query);

    List<Patient> findAllByOrderByCreatedAtDesc();
}

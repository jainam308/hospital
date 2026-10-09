package com.hospital.opd.service;

import com.hospital.opd.dto.DepartmentDTO;
import com.hospital.opd.dto.DoctorDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorService {
    List<DoctorDTO> getAllDoctors();
    List<DoctorDTO> getDoctorsByDepartment(Long departmentId);
    DoctorDTO getDoctorById(Long id);
    List<DepartmentDTO> getAllDepartments();
    boolean hasSlotConflict(String doctorName, LocalDateTime requestedTime);
}

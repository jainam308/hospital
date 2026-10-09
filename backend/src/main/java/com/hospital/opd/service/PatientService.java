package com.hospital.opd.service;

import com.hospital.opd.dto.PatientDTO;
import java.util.List;

public interface PatientService {
    PatientDTO createPatient(PatientDTO patientDTO);
    List<PatientDTO> getAllPatients();
    List<PatientDTO> searchPatients(String query);
    PatientDTO getPatientById(Long id);
}

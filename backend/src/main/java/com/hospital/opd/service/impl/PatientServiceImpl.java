package com.hospital.opd.service.impl;

import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.entity.Patient;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientDTO createPatient(PatientDTO patientDTO) {
        Patient patient = new Patient();
        patient.setName(patientDTO.getName().trim());
        patient.setGender(patientDTO.getGender());
        patient.setAge(patientDTO.getAge());
        patient.setPhoneNumber(patientDTO.getPhoneNumber().trim());
        patient.setBloodGroup(patientDTO.getBloodGroup() != null ? patientDTO.getBloodGroup().trim() : null);
        patient.setAllergies(patientDTO.getAllergies() != null ? patientDTO.getAllergies().trim() : null);
        patient.setChronicConditions(patientDTO.getChronicConditions() != null ? patientDTO.getChronicConditions().trim() : null);

        Patient saved = patientRepository.save(patient);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDTO> searchPatients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientRepository.searchByNameOrPhone(query.trim())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
        return mapToDTO(patient);
    }

    private PatientDTO mapToDTO(Patient patient) {
        return new PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                patient.getAge(),
                patient.getPhoneNumber(),
                patient.getBloodGroup(),
                patient.getAllergies(),
                patient.getChronicConditions(),
                patient.getCreatedAt()
        );
    }
}

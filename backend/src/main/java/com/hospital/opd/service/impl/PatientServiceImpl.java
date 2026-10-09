package com.hospital.opd.service.impl;

import com.hospital.opd.dto.PatientDTO;
import com.hospital.opd.entity.Patient;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private static final Set<String> VALID_BLOOD_GROUPS = Set.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientDTO createPatient(PatientDTO patientDTO) {
        String cleanPhone = patientDTO.getPhoneNumber().trim().replaceAll("[\\s\\-()]", "");
        String cleanEmail = patientDTO.getEmail() != null ? patientDTO.getEmail().trim().toLowerCase() : null;

        // 1. Business Logic: Prevent duplicate phone numbers
        Optional<Patient> existingByPhone = patientRepository.findByPhoneNumber(cleanPhone);
        if (existingByPhone.isPresent()) {
            Patient p = existingByPhone.get();
            throw new BadRequestException("A patient with phone number '" + cleanPhone + "' is already registered (Patient ID #" + p.getId() + ": " + p.getName() + "). Duplicate registrations are not permitted.");
        }

        // 2. Business Logic: Prevent duplicate emails
        if (cleanEmail != null && !cleanEmail.isEmpty()) {
            Optional<Patient> existingByEmail = patientRepository.findByEmailIgnoreCase(cleanEmail);
            if (existingByEmail.isPresent()) {
                Patient p = existingByEmail.get();
                throw new BadRequestException("A patient with email address '" + cleanEmail + "' is already registered (Patient ID #" + p.getId() + ": " + p.getName() + "). Duplicate registrations are not permitted.");
            }
        }

        // 3. Business Logic: Blood Group Validation
        String bloodGroup = null;
        if (patientDTO.getBloodGroup() != null && !patientDTO.getBloodGroup().trim().isEmpty()) {
            String bg = patientDTO.getBloodGroup().trim().toUpperCase();
            if (!VALID_BLOOD_GROUPS.contains(bg)) {
                throw new BadRequestException("Invalid blood group '" + bg + "'. Valid blood groups are: A+, A-, B+, B-, AB+, AB-, O+, O-");
            }
            bloodGroup = bg;
        }

        Patient patient = new Patient();
        patient.setName(patientDTO.getName().trim());
        patient.setGender(patientDTO.getGender());
        patient.setAge(patientDTO.getAge());
        patient.setPhoneNumber(cleanPhone);
        patient.setEmail(cleanEmail != null && !cleanEmail.isEmpty() ? cleanEmail : null);
        patient.setBloodGroup(bloodGroup);
        patient.setAllergies(patientDTO.getAllergies() != null && !patientDTO.getAllergies().trim().isEmpty() ? patientDTO.getAllergies().trim() : null);
        patient.setChronicConditions(patientDTO.getChronicConditions() != null && !patientDTO.getChronicConditions().trim().isEmpty() ? patientDTO.getChronicConditions().trim() : null);

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
                patient.getEmail(),
                patient.getBloodGroup(),
                patient.getAllergies(),
                patient.getChronicConditions(),
                patient.getCreatedAt()
        );
    }
}

package com.hospital.opd.service.impl;

import com.hospital.opd.dto.ConsultationRequestDTO;
import com.hospital.opd.dto.ConsultationResponseDTO;
import com.hospital.opd.dto.PrescriptionItemDTO;
import com.hospital.opd.entity.*;
import com.hospital.opd.exception.BadRequestException;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.ConsultationRepository;
import com.hospital.opd.repository.PatientRepository;
import com.hospital.opd.service.BillingService;
import com.hospital.opd.service.ConsultationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private static final Pattern BP_PATTERN = Pattern.compile("^(\\d{2,3})\\s*/\\s*(\\d{2,3})(?:\\s*mmHg)?$", Pattern.CASE_INSENSITIVE);

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final BillingService billingService;

    public ConsultationServiceImpl(ConsultationRepository consultationRepository,
                                   AppointmentRepository appointmentRepository,
                                   PatientRepository patientRepository,
                                   BillingService billingService) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.billingService = billingService;
    }

    @Override
    public ConsultationResponseDTO createConsultation(ConsultationRequestDTO requestDTO) {
        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + requestDTO.getAppointmentId()));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot conduct a consultation for a CANCELLED appointment.");
        }

        if (consultationRepository.existsByAppointmentId(requestDTO.getAppointmentId())) {
            throw new BadRequestException("Consultation has already been recorded for this appointment");
        }

        // 1. Business Logic: Blood Pressure Clinical Sanity Validation
        String bp = requestDTO.getBloodPressure() != null ? requestDTO.getBloodPressure().trim() : "";
        Matcher bpMatcher = BP_PATTERN.matcher(bp);
        if (!bpMatcher.matches()) {
            throw new BadRequestException("Invalid blood pressure format: '" + bp + "'. Expected format: '120/80' or '120/80 mmHg'.");
        }
        int systolic = Integer.parseInt(bpMatcher.group(1));
        int diastolic = Integer.parseInt(bpMatcher.group(2));
        if (systolic < 70 || systolic > 260) {
            throw new BadRequestException("Systolic blood pressure (" + systolic + " mmHg) is outside clinical limits (70 - 260 mmHg).");
        }
        if (diastolic < 40 || diastolic > 160) {
            throw new BadRequestException("Diastolic blood pressure (" + diastolic + " mmHg) is outside clinical limits (40 - 160 mmHg).");
        }
        if (systolic <= diastolic) {
            throw new BadRequestException("Systolic pressure (" + systolic + ") must be greater than diastolic pressure (" + diastolic + ").");
        }

        // 2. Business Logic: Heart Rate Sanity Check
        if (requestDTO.getHeartRate() != null) {
            int hr = requestDTO.getHeartRate();
            if (hr < 35 || hr > 220) {
                throw new BadRequestException("Heart rate (" + hr + " bpm) is outside plausible clinical range (35 - 220 bpm).");
            }
        }

        // 3. Business Logic: Temperature Sanity Check
        if (requestDTO.getTemperature() != null) {
            double temp = requestDTO.getTemperature();
            if (temp < 94.0 || temp > 108.0) {
                throw new BadRequestException("Body temperature (" + temp + " °F) is outside plausible clinical range (94.0°F - 108.0°F).");
            }
        }

        Consultation consultation = new Consultation();
        consultation.setAppointment(appointment);
        consultation.setPatient(appointment.getPatient());
        consultation.setBloodPressure(systolic + "/" + diastolic + " mmHg");
        consultation.setHeartRate(requestDTO.getHeartRate());
        consultation.setTemperature(requestDTO.getTemperature());
        consultation.setNotes(requestDTO.getNotes().trim());
        consultation.setConsultationDate(LocalDateTime.now());

        // Process itemized prescription medications
        if (requestDTO.getPrescriptionItems() != null && !requestDTO.getPrescriptionItems().isEmpty()) {
            for (PrescriptionItemDTO itemDTO : requestDTO.getPrescriptionItems()) {
                if (itemDTO.getMedicineName() != null && !itemDTO.getMedicineName().trim().isEmpty()) {
                    PrescriptionItem item = new PrescriptionItem();
                    item.setMedicineName(itemDTO.getMedicineName().trim());
                    item.setDosage(itemDTO.getDosage() != null ? itemDTO.getDosage().trim() : "Standard");
                    item.setFrequency(itemDTO.getFrequency() != null ? itemDTO.getFrequency().trim() : "1-0-1");
                    item.setDuration(itemDTO.getDuration() != null ? itemDTO.getDuration().trim() : "5 days");
                    item.setInstructions(itemDTO.getInstructions() != null ? itemDTO.getInstructions().trim() : "After food");
                    consultation.addPrescriptionItem(item);
                }
            }
        }

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        Consultation saved = consultationRepository.save(consultation);

        // Auto-generate Bill so patient can settle via Razorpay or Cash
        try {
            billingService.generateBillForAppointment(appointment.getId());
        } catch (Exception ignored) {
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getConsultationsByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }
        return consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultationByAppointment(Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment ID: " + appointmentId));
        return mapToDTO(consultation);
    }

    private ConsultationResponseDTO mapToDTO(Consultation c) {
        ConsultationResponseDTO dto = new ConsultationResponseDTO();
        dto.setId(c.getId());
        dto.setAppointmentId(c.getAppointment().getId());
        dto.setPatientId(c.getPatient().getId());
        dto.setPatientName(c.getPatient().getName());
        dto.setPatientPhone(c.getPatient().getPhoneNumber());
        dto.setBloodGroup(c.getPatient().getBloodGroup());
        dto.setAllergies(c.getPatient().getAllergies());
        dto.setDoctorName(c.getAppointment().getDoctorName());
        dto.setBloodPressure(c.getBloodPressure());
        dto.setHeartRate(c.getHeartRate());
        dto.setTemperature(c.getTemperature());
        dto.setNotes(c.getNotes());
        dto.setConsultationDate(c.getConsultationDate());

        List<PrescriptionItemDTO> items = new ArrayList<>();
        if (c.getPrescriptionItems() != null) {
            for (PrescriptionItem pi : c.getPrescriptionItems()) {
                items.add(new PrescriptionItemDTO(
                        pi.getId(),
                        pi.getMedicineName(),
                        pi.getDosage(),
                        pi.getFrequency(),
                        pi.getDuration(),
                        pi.getInstructions()
                ));
            }
        }
        dto.setPrescriptionItems(items);
        return dto;
    }
}

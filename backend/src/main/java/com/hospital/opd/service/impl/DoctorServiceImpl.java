package com.hospital.opd.service.impl;

import com.hospital.opd.dto.DepartmentDTO;
import com.hospital.opd.dto.DoctorDTO;
import com.hospital.opd.entity.Appointment;
import com.hospital.opd.entity.AppointmentStatus;
import com.hospital.opd.entity.Department;
import com.hospital.opd.entity.Doctor;
import com.hospital.opd.exception.ResourceNotFoundException;
import com.hospital.opd.repository.AppointmentRepository;
import com.hospital.opd.repository.DepartmentRepository;
import com.hospital.opd.repository.DoctorRepository;
import com.hospital.opd.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             DepartmentRepository departmentRepository,
                             AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDoctorDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndActiveTrue(departmentId)
                .stream()
                .map(this::mapToDoctorDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));
        return mapToDoctorDTO(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(d -> new DepartmentDTO(d.getId(), d.getName(), d.getCode(), d.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSlotConflict(String doctorName, LocalDateTime requestedTime) {
        LocalDateTime windowStart = requestedTime.minusMinutes(29);
        LocalDateTime windowEnd = requestedTime.plusMinutes(29);

        List<Appointment> existing = appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(windowStart, windowEnd);
        return existing.stream()
                .anyMatch(a -> a.getDoctorName().equalsIgnoreCase(doctorName) && a.getStatus() == AppointmentStatus.SCHEDULED);
    }

    private DoctorDTO mapToDoctorDTO(Doctor doctor) {
        return new DoctorDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getDepartment().getId(),
                doctor.getDepartment().getName(),
                doctor.getSpecialization(),
                doctor.getConsultationFee(),
                doctor.getRoomNumber(),
                doctor.getShift(),
                doctor.getMaxDailyQuota(),
                doctor.getSlotDurationMinutes(),
                doctor.getEmail(),
                doctor.getPhone(),
                doctor.isActive()
        );
    }
}

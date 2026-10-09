package com.hospital.opd.controller;

import com.hospital.opd.dto.DepartmentDTO;
import com.hospital.opd.dto.DoctorDTO;
import com.hospital.opd.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> getDoctors(@RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(doctorService.getDoctorsByDepartment(departmentId));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartments() {
        return ResponseEntity.ok(doctorService.getAllDepartments());
    }

    @GetMapping("/doctors/check-conflict")
    public ResponseEntity<Map<String, Boolean>> checkSlotConflict(
            @RequestParam String doctorName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime) {
        boolean hasConflict = doctorService.hasSlotConflict(doctorName, appointmentTime);
        return ResponseEntity.ok(Collections.singletonMap("conflict", hasConflict));
    }
}

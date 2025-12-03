package com.health.doctor.controller;

import com.health.doctor.model.Doctor;
import com.health.doctor.model.DoctorPatient;
import com.health.doctor.service.DoctorService;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private final DoctorService doctorService;

    @Autowired
    private final UserRepository userRepository;

    public DoctorController(DoctorService doctorService, UserRepository userRepository) {
        this.doctorService = doctorService;
        this.userRepository = userRepository;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // Register doctor (clinicId optional)
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('DOCTOR','SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<Doctor> registerDoctor(
            @RequestParam(required = false) Long clinicId,
            @RequestBody Doctor doctor,
            Authentication authentication
    ) {
        Doctor savedDoctor = doctorService.registerDoctor(clinicId, doctor, authentication);
        return ResponseEntity.ok(savedDoctor);
    }

    // Doctor self update endpoint
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR','SUPER_ADMIN')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable("id") Long id,
                                               @RequestBody Doctor dto,
                                               Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Doctor doctor = doctorService.getDoctor(id);
        if (!doctor.getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Doctor can only update their own profile");
        }

        return ResponseEntity.ok(doctorService.updateDoctor(id, dto));
    }

    //self detail
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(doctorService.getDoctorByUser(authentication));
    }

    // Link patient to doctor (start treatment)
    @PostMapping("/patients/link")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> linkPatient(@RequestBody DoctorPatient relationship, Authentication authentication) {
        return doctorService.linkPatient(relationship, authentication);
    }

    // Get all patients for logged-in doctor
    @GetMapping("/patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getMyPatients(
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        return doctorService.getMyPatients(status, authentication);
    }

    // Update treatment status (ONGOING -> COMPLETED)
    @PutMapping("/patients/{relationshipId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateTreatmentStatus(
            @PathVariable Long relationshipId,
            @RequestParam String status,
            Authentication authentication
    ) {
        return doctorService.updateTreatmentStatus(relationshipId, status, authentication);
    }

    //link doctor to clinic
    @PostMapping("/link-clinic")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<?> linkDoctorToClinic(
            @RequestParam Long doctorId,
            @RequestParam Long clinicId,
            Authentication authentication
    ) {
        return doctorService.linkDoctorToClinic(doctorId, clinicId, authentication);
    }

    // Search doctors by name or email
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> searchDoctors(@RequestParam String query) {
        return ResponseEntity.ok(doctorService.searchDoctors(query));
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // Public endpoint to view a doctor profile
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctor(@PathVariable("id") Long id) {
        return ResponseEntity.ok(doctorService.getDoctor(id));
    }
}

package com.health.clinic.controller;

import com.health.clinic.dto.ClinicRequestDTO;
import com.health.clinic.dto.ClinicResponseDTO;
import com.health.clinic.model.Clinic;
import com.health.clinic.service.ClinicService;
import com.health.doctor.dto.DoctorResponseDTO;
import com.health.doctor.model.Doctor;
import com.health.doctor.service.DoctorService;
import com.health.lab.dto.LinkedLabDTO;
import com.health.patient.dto.LinkedPatientDTO;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clinic")
public class ClinicController {
    @Autowired
    private final ClinicService clinicService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final DoctorService doctorService;

    public ClinicController(ClinicService clinicService, UserRepository userRepository, DoctorService doctorService) {
        this.clinicService = clinicService;
        this.userRepository = userRepository;
        this.doctorService = doctorService;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // register a new clinic
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CLINIC_ADMIN')")
    public ResponseEntity<ClinicResponseDTO> registerClinic(
            @RequestBody ClinicRequestDTO clinicRequest,
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        ClinicResponseDTO response = clinicService.registerClinic(clinicRequest, user.getUserId());
        return ResponseEntity.ok(response);
    }

    //get personal data
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<ClinicResponseDTO> getMyClinic(Authentication authentication) {
        return ResponseEntity.ok(clinicService.getClinicByOwner(authentication));
    }

    // update clinic detail
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLINIC_ADMIN')")
    public ResponseEntity<ClinicResponseDTO> updateClinic(@PathVariable Long id, @RequestBody ClinicRequestDTO clinic) {
        return ResponseEntity.ok(clinicService.updateClinic(id, clinic));
    }

    //get all linked labs to the clinic
    @GetMapping("/labs")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<List<LinkedLabDTO>> getLinkedLabs(Authentication auth) {
        // service should infer clinicId from auth, or map user -> clinic
        List<LinkedLabDTO> labs = clinicService.getLinkedLabs(auth);
        return ResponseEntity.ok(labs);
    }

    //unlink doctor to clinic
    @PostMapping("/unlink-doctor")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<?> unlinkDoctor(@RequestParam Long doctorId, Authentication auth) {
        boolean removed = clinicService.unlinkDoctorFromClinic(doctorId, auth);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor not linked to clinic"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Doctor unlinked from clinic successfully",
                "doctorId", doctorId
        ));
    }

    //unlink lab to clinic
    @PostMapping("/unlink-lab")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<?> unlinkLab(@RequestParam Long labId, Authentication auth) {
        boolean removed = clinicService.unlinkLabFromClinic(labId, auth);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Lab not linked to clinic"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Lab unlinked from clinic successfully",
                "labId", labId
        ));
    }

    // unlink patient to clinic
    @PostMapping("/unlink/{patientId}")
    public ResponseEntity<?> unlinkPatient(@PathVariable Long patientId, Authentication auth) {
        String username = auth != null ? auth.getName() : "anonymous";
        return clinicService.unlinkPatient(patientId, username);
    }

    // Authenticated clinic admin: their own clinic's doctors
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<List<DoctorResponseDTO>> getClinicDoctors(Authentication authentication) {
        return ResponseEntity.ok(doctorService.getClinicDoctors(authentication));
    }

    // link patient to clinic
    @PostMapping("/link/{patientId}")
    public ResponseEntity<?> linkPatient(@PathVariable Long patientId, Authentication auth) {
        String username = auth != null ? auth.getName() : "anonymous";
        return clinicService.linkPatient(patientId, username);
    }

    //get all patient for the clinic
    @GetMapping("/patients")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<List<LinkedPatientDTO>> getLinkedPatients(Authentication auth) {
        return ResponseEntity.ok(clinicService.getLinkedPatients(auth));
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // get clinic detail using id
    @GetMapping("/{id}")
    public ResponseEntity<Clinic> getClinicById(@PathVariable Long id) {
        Clinic clinic = clinicService.getClinicById(id);
        return ResponseEntity.ok(clinic);
    }

    // get all clinic
    @GetMapping("/all")
    public ResponseEntity<List<Clinic>> getAllClinics() {
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    // delete clinic detail
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<String> deleteClinic(@PathVariable Long id) {
        clinicService.deleteClinic(id);
        return ResponseEntity.ok("Clinic deleted successfully");
    }

    // SUPER_ADMIN: any clinic's doctors by clinic ID
    @GetMapping("/{id}/doctors")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Doctor>> getClinicDoctorsById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getClinicDoctorsById(id));
    }
}


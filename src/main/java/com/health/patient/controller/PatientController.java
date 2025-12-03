package com.health.patient.controller;

import com.health.patient.dto.PatientRegistrationRequest;
import com.health.patient.model.Patient;
import com.health.patient.model.PatientHistory;
import com.health.patient.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patient")
public class PatientController {
    @Autowired
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // register patient
    @PostMapping("/register")
    public ResponseEntity<?> registerPatient(@RequestBody PatientRegistrationRequest request) {
        return patientService.registerPatient(request);
    }

    // update patient details
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        return patientService.updatePatient(id, patient, username);
    }

    // get patient basic report
    @GetMapping("/commonReport/{patientId}")
    public ResponseEntity<?> getCommonReport(@PathVariable Long patientId) {
        return patientService.getCommonReport(patientId);
    }

    // get patient complete history
    @GetMapping("/history/{patientId}")
    public ResponseEntity<?> getHistory(@PathVariable Long patientId) {
        return patientService.fetchHistory(patientId);
    }

    // Get patients linked to a specific clinic, optionally filtered by name
    @GetMapping("/search/{clinicId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getPatientsByClinic(
            @PathVariable Long clinicId,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(patientService.getPatientsByClinic(clinicId, query));
    }

    //get self details
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        return patientService.getPatientByUser(authentication);
    }

    // add patient history by doctor
    @PostMapping("/history/add")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addPatientHistory(@RequestBody PatientHistory history, Authentication authentication) {
        return patientService.addPatientHistory(history, authentication);
    }

    // get patient all linked clinics
    @GetMapping("/linked-clinics/{patientId}")
    public ResponseEntity<?> getLinkedClinics(@PathVariable Long patientId) {
        return patientService.getLinkedClinics(patientId);
    }

//    //get all linked doctor for patient
//    @GetMapping("/linked-doctors")
//    public ResponseEntity<?> getLinkedDoctors(Authentication authentication){
//
//    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    //get patient's detail
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('LAB_ADMIN', 'CLINIC_ADMIN', 'DOCTOR')")
    public ResponseEntity<?> searchPatients(@RequestParam String query) {
        return ResponseEntity.ok(patientService.searchPatients(query));
    }

    // get patient by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatient(@PathVariable Long id) {
        return patientService.getPatient(id);
    }


    // ----------------------------PENDING---------------------------------

    // 🕓 POST /patient/consent/{patientId}
    @PostMapping("/consent/{patientId}")
    public ResponseEntity<?> manageConsent(@PathVariable Long patientId) {
        return patientService.manageConsent(patientId);
    }

    // 🕓 POST /patient/addDependent
    @PostMapping("/addDependent")
    public ResponseEntity<?> addDependent(Authentication auth, @RequestBody Patient dependent) {
        Long userId = 0L; // You’ll later map this from authenticated user
        return patientService.addDependent(userId, dependent);
    }

    // 🕓 GET /patient/dependents
    @GetMapping("/dependents")
    public ResponseEntity<?> getDependents(Authentication auth) {
        Long userId = 0L; // Map from authenticated patient later
        return patientService.getDependents(userId);
    }
}


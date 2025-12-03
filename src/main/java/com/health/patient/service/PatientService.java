package com.health.patient.service;

import com.health.clinic.model.Clinic;
import com.health.clinic.repository.ClinicRepository;
import com.health.doctor.model.Doctor;
import com.health.doctor.repository.DoctorRepository;
import com.health.patient.dto.PatientRegistrationRequest;
import com.health.patient.model.Patient;
import com.health.patient.model.PatientClinicLink;
import com.health.patient.model.PatientHealthSummary;
import com.health.patient.model.PatientHistory;
import com.health.patient.repository.PatientClinicRepository;
import com.health.patient.repository.PatientHealthSummaryRepository;
import com.health.patient.repository.PatientHistoryRepository;
import com.health.patient.repository.PatientRepository;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PatientHealthSummaryRepository patientHealthSummaryRepository;

    @Autowired
    private final PatientHistoryRepository patientHistoryRepository;

    @Autowired
    private final PatientClinicRepository patientClinicRepository;

    @Autowired
    private final ClinicRepository clinicRepository;

    @Autowired
    private final DoctorRepository doctorRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository, PatientHealthSummaryRepository patientHealthSummaryRepository, PatientHistoryRepository patientHistoryRepository, PatientClinicRepository patientClinicRepository, ClinicRepository clinicRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.patientHealthSummaryRepository = patientHealthSummaryRepository;
        this.patientHistoryRepository = patientHistoryRepository;
        this.patientClinicRepository = patientClinicRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // register the patient
    public ResponseEntity<?> registerPatient(PatientRegistrationRequest request) {
        try {
            // Get user by email from request
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid user: " + request.getEmail());
            }

            User user = userOpt.get();

            // Create patient object
            Patient patient = new Patient();
            patient.setUserId(user.getUserId());
            patient.setName(request.getName());
            patient.setDob(LocalDate.parse(request.getDob()));
            patient.setGender(request.getGender());
            patient.setBloodGroup(request.getBloodGroup());
            patient.setHeightCm(request.getHeightCm());
            patient.setWeightKg(request.getWeightKg());
            patient.setAllergies(request.getAllergies());

            patientRepository.save(patient);

            user.setProfileCompleted(true);
            userRepository.save(user);

            return ResponseEntity.ok("Patient registered successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering patient: " + e.getMessage());
        }
    }

    // update the patient detail only self
    public ResponseEntity<?> updatePatient(Long id, Patient updated, String username) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(403).body("Unauthorized user");
            }
            Long loggedInUserId = userOpt.get().getUserId();

            Optional<Patient> optional = patientRepository.findById(id);
            if (optional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Patient existing = optional.get();

            // Authorization check — only owner can update
            if (!existing.getUserId().equals(loggedInUserId)) {
                return ResponseEntity.status(403).body("You are not allowed to update this patient's details");
            }

            // Update allowed
            existing.setName(updated.getName());
            existing.setDob(updated.getDob());
            existing.setGender(updated.getGender());
            existing.setBloodGroup(updated.getBloodGroup());
            existing.setHeightCm(updated.getHeightCm());
            existing.setWeightKg(updated.getWeightKg());
            existing.setAllergies(updated.getAllergies());
            existing.setUpdatedAt(java.time.LocalDateTime.now());

            patientRepository.save(existing);
            return ResponseEntity.ok("Patient updated successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating patient: " + e.getMessage());
        }
    }

    //fetch patient complete medical history
    public ResponseEntity<?> fetchHistory(Long patientId) {
        try {
            List<PatientHistory> historyList = patientHistoryRepository.findByPatientId(patientId);

            if (historyList.isEmpty()) {
                return ResponseEntity.ok("No history found for this patient.");
            }
            return ResponseEntity.ok(historyList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching history: " + e.getMessage());
        }
    }

    //summarised report
    public ResponseEntity<?> getCommonReport(Long patientId) {
        try {
            Optional<PatientHealthSummary> optionalSummary = patientHealthSummaryRepository.findByPatientId(patientId);


            if (optionalSummary.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No health summary found"));
            }

            PatientHealthSummary summary = optionalSummary.get();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("patientId", summary.getPatientId());
            response.put("bloodPressure", summary.getBloodPressure());
            response.put("sugarLevel", summary.getSugarLevel());
            response.put("cholesterolLevel", summary.getCholesterolLevel());
            response.put("heartRate", summary.getHeartRate());
            response.put("existingConditions", summary.getExistingConditions());
            response.put("lastUpdated", summary.getLastUpdated());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error retrieving health summary: " + e.getMessage());
        }
    }


    //get user self data
    public ResponseEntity<?> getPatientByUser(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        Optional<Patient> patientOpt = patientRepository.findByUserId(userOpt.get().getUserId());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Patient profile not found");
        }

        return ResponseEntity.ok(patientOpt.get());
    }

    //add patient history
    public ResponseEntity<?> addPatientHistory(PatientHistory history, Authentication authentication) {
        try {
            // Get authenticated doctor
            String email = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(403).body("User not found");
            }

            Long userId = userOpt.get().getUserId();

            // Get doctor by userId
            Optional<Doctor> doctorOpt = doctorRepository.findByUserId(userId);
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.status(403).body("Doctor profile not found");
            }

            Doctor doctor = doctorOpt.get();

            // Set doctor and clinic info
            history.setDoctorId(doctor.getDoctorId());
            history.setClinicId(doctor.getClinicId());
            history.setCreatedAt(OffsetDateTime.now());

            // Validate patient exists
            if (!patientRepository.existsById(history.getPatientId())) {
                return ResponseEntity.badRequest().body("Patient not found");
            }

            PatientHistory saved = patientHistoryRepository.save(history);
            return ResponseEntity.ok(Map.of(
                    "message", "Patient history added successfully",
                    "recordId", saved.getRecordId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error adding patient history: " + e.getMessage());
        }
    }

    public List<Patient> getPatientsByClinic(Long clinicId, String query) {
        // Fetch all ACTIVE patient links for the given clinic
        List<PatientClinicLink> links = patientClinicRepository.findByClinicIdAndStatus(clinicId, "ACTIVE");

        // Extract patient IDs
        List<Long> patientIds = links.stream()
                .map(PatientClinicLink::getPatientId)
                .toList();

        if (patientIds.isEmpty()) {
            return List.of(); // No patients linked
        }

        // If query provided, filter by name (case-insensitive)
        if (query != null && !query.isBlank()) {
            return patientRepository.findByPatientIdInAndNameContainingIgnoreCase(patientIds, query);
        }

        // Otherwise, return all patients under this clinic
        return patientRepository.findByPatientIdIn(patientIds);
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    public List<Patient> searchPatients(String query) {
        return patientRepository.findByNameContainingIgnoreCase(query);
    }

    // get patient by id
    public ResponseEntity<?> getPatient(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        return patient.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    public ResponseEntity<?> getLinkedClinics(Long patientId) {
        try {
            List<PatientClinicLink> links = patientClinicRepository.findByPatientId(patientId);
            if (links.isEmpty()) {
                return ResponseEntity.ok("No linked clinics found for this patient.");
            }

            // fetch clinic details
            List<Long> clinicIds = links.stream()
                    .map(PatientClinicLink::getClinicId)
                    .collect(Collectors.toList());

            List<Clinic> clinics = clinicRepository.findAllById(clinicIds);
            return ResponseEntity.ok(clinics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching linked clinics: " + e.getMessage());
        }
    }

    // 🕓 Under progress endpoints
    public ResponseEntity<?> manageConsent(Long patientId) {
        return ResponseEntity.ok("Under Progress");
    }

    public ResponseEntity<?> addDependent(Long userId, Patient dependent) {
        return ResponseEntity.ok("Under Progress");
    }

    public ResponseEntity<?> getDependents(Long userId) {
        return ResponseEntity.ok("Under Progress");
    }
}


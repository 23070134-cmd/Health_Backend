package com.health.doctor.service;

import com.health.clinic.model.Clinic;
import com.health.doctor.dto.DoctorResponseDTO;
import com.health.doctor.mapper.DoctorMapper;
import com.health.doctor.model.Doctor;
import com.health.doctor.model.DoctorPatient;
import com.health.doctor.repository.DoctorPatientRepository;
import com.health.doctor.repository.DoctorRepository;
import com.health.clinic.repository.ClinicRepository;
import com.health.patient.model.Patient;
import com.health.patient.repository.PatientRepository;
import com.health.user.model.Role;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class DoctorService {

    @Autowired
    private final DoctorRepository doctorRepository;

    @Autowired
    private final ClinicRepository clinicRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final DoctorPatientRepository doctorPatientRepository;

    @Autowired
    private final PatientRepository patientRepository;

    @Autowired
    private final DoctorMapper doctorMapper;

    public DoctorService(DoctorRepository doctorRepository, ClinicRepository clinicRepository, UserRepository userRepository, DoctorPatientRepository doctorPatientRepository, PatientRepository patientRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.clinicRepository = clinicRepository;
        this.userRepository = userRepository;
        this.doctorPatientRepository = doctorPatientRepository;
        this.patientRepository = patientRepository;
        this.doctorMapper = doctorMapper;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//


    //register a doctor under a specific clinic
    public Doctor registerDoctor(Long clinicId, Doctor doctor, Authentication authentication) {
        // Get authenticated user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // Set the userId for the doctor
        doctor.setUserId(user.getUserId());

        // If clinicId provided, validate it exists and assign
        if (clinicId != null) {
            if (!clinicRepository.existsById(clinicId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
            }
            doctor.setClinicId(clinicId);
        } else {
            doctor.setClinicId(null);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        user.setProfileCompleted(true);
        userRepository.save(user);

        return savedDoctor;
    }

    //update doctor details
    public Doctor updateDoctor(Long id, Doctor dto) {
        Doctor existing = getDoctor(id);

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getSpecialization() != null) existing.setSpecialization(dto.getSpecialization());
        if (dto.getLicenseNumber() != null) existing.setLicenseNumber(dto.getLicenseNumber());
        if (dto.getExperienceYears() != null) existing.setExperienceYears(dto.getExperienceYears());
        if (dto.getGender() != null) existing.setGender(dto.getGender());
        if (dto.getAvailableDays() != null) existing.setAvailableDays(dto.getAvailableDays());

        return doctorRepository.save(existing);
    }

    // get self data
    public Doctor getDoctorByUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return doctorRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    public List<DoctorResponseDTO> getClinicDoctors(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Clinic clinic = clinicRepository.findByOwnerUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No clinic found for this admin"));

        List<Doctor> doctors = doctorRepository.findAllByClinicId(clinic.getClinicId());
        return doctorMapper.toResponseList(doctors);
    }

    // Link patient to doctor
    public ResponseEntity<?> linkPatient(DoctorPatient relationship, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            Doctor doctor = doctorRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

            // Check if patient exists
            if (!patientRepository.existsById(relationship.getPatientId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient not found"));
            }

            // Check if relationship already exists
            Optional<DoctorPatient> existing = doctorPatientRepository
                    .findByDoctorIdAndPatientId(doctor.getDoctorId(), relationship.getPatientId());

            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Patient already linked to this doctor"));
            }

            // Create new relationship
            relationship.setDoctorId(doctor.getDoctorId());
            relationship.setStatus("ONGOING");
            relationship.setStartDate(LocalDate.now());
            relationship.setCreatedAt(OffsetDateTime.now());
            relationship.setUpdatedAt(OffsetDateTime.now());

            DoctorPatient saved = doctorPatientRepository.save(relationship);

            return ResponseEntity.ok(Map.of(
                    "message", "Patient linked successfully",
                    "relationshipId", saved.getRelationshipId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error linking patient: " + e.getMessage()));
        }
    }

    // Get patients for doctor with optional status filter
    public ResponseEntity<?> getMyPatients(String status, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            Doctor doctor = doctorRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

            List<DoctorPatient> relationships;

            if (status != null && !status.isEmpty()) {
                relationships = doctorPatientRepository.findByDoctorIdAndStatus(doctor.getDoctorId(), status);
            } else {
                relationships = doctorPatientRepository.findByDoctorId(doctor.getDoctorId());
            }

            // Fetch patient details for each relationship
            List<Map<String, Object>> result = new ArrayList<>();
            for (DoctorPatient rel : relationships) {
                Optional<Patient> patientOpt = patientRepository.findById(rel.getPatientId());
                if (patientOpt.isPresent()) {
                    Patient patient = patientOpt.get();
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("relationshipId", rel.getRelationshipId());
                    data.put("patientId", patient.getPatientId());
                    data.put("name", patient.getName());
                    data.put("dob", patient.getDob());
                    data.put("gender", patient.getGender());
                    data.put("bloodGroup", patient.getBloodGroup());
                    data.put("status", rel.getStatus());
                    data.put("startDate", rel.getStartDate());
                    data.put("endDate", rel.getEndDate());
                    data.put("notes", rel.getNotes());
                    result.add(data);
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error fetching patients: " + e.getMessage()));
        }
    }

    // Update treatment status
    public ResponseEntity<?> updateTreatmentStatus(Long relationshipId, String status, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            Doctor doctor = doctorRepository.findByUserId(user.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));

            DoctorPatient relationship = doctorPatientRepository.findById(relationshipId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relationship not found"));

            // Verify the relationship belongs to this doctor
            if (!relationship.getDoctorId().equals(doctor.getDoctorId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            // Update status
            relationship.setStatus(status);
            relationship.setUpdatedAt(OffsetDateTime.now());

            if ("COMPLETED".equals(status) && relationship.getEndDate() == null) {
                relationship.setEndDate(LocalDate.now());
            }

            doctorPatientRepository.save(relationship);

            return ResponseEntity.ok(Map.of("message", "Treatment status updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error updating status: " + e.getMessage()));
        }
    }

    // Link existing doctor to a clinic
    public ResponseEntity<?> linkDoctorToClinic(Long doctorId, Long clinicId, Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            // Role-based restriction
            boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;
            boolean isClinicAdmin = user.getRole() == Role.CLINIC_ADMIN;

            if (!isSuperAdmin && !isClinicAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized to link doctor"));
            }

            // Validate doctor existence
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));

            // Validate clinic existence
            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

            // Update doctor’s clinicId
            doctor.setClinicId(clinic.getClinicId());
            doctorRepository.save(doctor);

            return ResponseEntity.ok(Map.of(
                    "message", "Doctor linked to clinic successfully",
                    "doctorId", doctor.getDoctorId(),
                    "clinicId", clinic.getClinicId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error linking doctor to clinic: " + e.getMessage()));
        }
    }

    public List<Map<String, Object>> searchDoctors(String query) {
        System.out.println("🔍 Searching doctors with query: " + query);
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCase(query);
        System.out.println("✅ Found doctors count: " + doctors.size());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Doctor doctor : doctors) {
            System.out.println("🩺 Doctor found: " + doctor.getName());
            Optional<User> userOpt = userRepository.findById(doctor.getUserId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("doctorId", doctor.getDoctorId());
            map.put("name", doctor.getName());
            map.put("specialization", doctor.getSpecialization());
            map.put("email", userOpt.map(User::getEmail).orElse("N/A"));
            map.put("clinicId", doctor.getClinicId());
            result.add(map);
        }

        return result;
    }




    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//


    //get doctor by id
    public Doctor getDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    // For SUPER_ADMIN fetching any clinic's doctors by ID
    public List<Doctor> getClinicDoctorsById(Long clinicId) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        }
        return doctorRepository.findAllByClinicId(clinicId);
    }
}

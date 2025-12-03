package com.health.clinic.service;

import com.health.clinic.dto.ClinicRequestDTO;
import com.health.clinic.dto.ClinicResponseDTO;
import com.health.clinic.mapper.ClinicMapper;
import com.health.clinic.model.Clinic;
import com.health.clinic.repository.ClinicRepository;
import com.health.doctor.model.Doctor;
import com.health.doctor.repository.DoctorRepository;
import com.health.lab.dto.LinkedLabDTO;
import com.health.lab.model.Lab;
import com.health.lab.repository.LabRepository;
import com.health.patient.dto.LinkedPatientDTO;
import com.health.patient.model.Patient;
import com.health.patient.model.PatientClinicLink;
import com.health.patient.repository.PatientClinicRepository;
import com.health.patient.repository.PatientRepository;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ClinicService {
    @Autowired
    private final ClinicRepository clinicRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PatientClinicRepository patientClinicRepository;
    @Autowired
    private final PatientRepository patientRepository;
    @Autowired
    private final LabRepository labRepository;
    @Autowired
    private final DoctorRepository doctorRepository;
    @Autowired
    private ClinicMapper clinicMapper;

    public ClinicService(ClinicRepository clinicRepository, UserRepository userRepository, PatientClinicRepository patientClinicRepository, PatientRepository patientRepository, LabRepository labRepository, DoctorRepository doctorRepository) {
        this.clinicRepository = clinicRepository;
        this.userRepository = userRepository;
        this.patientClinicRepository = patientClinicRepository;
        this.patientRepository = patientRepository;
        this.labRepository = labRepository;
        this.doctorRepository = doctorRepository;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // Register new clinic
    public ClinicResponseDTO registerClinic(ClinicRequestDTO dto, Long ownerUserId) {
        // Convert DTO → Entity
        Clinic clinic = clinicMapper.getClinicEntity(dto);
        clinic.setOwnerUserId(ownerUserId);

        // Validation
        if (!userRepository.existsById(ownerUserId)) {
            throw new RuntimeException("Owner user not found");
        }

        Clinic savedClinic = clinicRepository.save(clinic);

        // Update user profile flag
        User user = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfileCompleted(true);
        userRepository.save(user);

        // Convert Entity → Response DTO
        ClinicResponseDTO response = clinicMapper.getClinicResponseDTO(savedClinic);

        return response;
    }

    //get clinic by owner
    public ClinicResponseDTO getClinicByOwner(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Clinic clinic = clinicRepository.findByOwnerUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No clinic found for this admin"));

        ClinicResponseDTO responseDTO = clinicMapper.getClinicResponseDTO(clinic);
        return responseDTO;
    }

    // Update clinic info
    public ClinicResponseDTO updateClinic(Long id, ClinicRequestDTO clinicDetails) {
        Clinic clinic = getClinicById(id);
        clinic.setName(clinicDetails.getName());
        clinic.setEmail(clinicDetails.getEmail());
        clinic.setPhone(clinicDetails.getPhone());
        clinic.setAddress(clinicDetails.getAddress());
        clinic.setCity(clinicDetails.getCity());
        clinic.setState(clinicDetails.getState());
        clinic.setPincode(clinicDetails.getPincode());
        clinic.setUpdatedAt(Instant.now());
        clinicRepository.save(clinic);
        return clinicMapper.getClinicResponseDTO(clinic);
    }

    //link patient to clinic
    public ResponseEntity<?> linkPatient(Long patientId, String email) {
        try {
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with email: " + email);
            }
            var clinicOpt = clinicRepository.findByOwnerUserId(userOpt.get().getUserId());
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Clinic not found for user: " + email);
            }
            Long clinicId = clinicOpt.get().getClinicId();

            // In ClinicService.linkPatient(...)
            var existingLinkOpt = patientClinicRepository.findByPatientIdAndClinicId(patientId, clinicId);

            if (existingLinkOpt.isPresent()) {
                PatientClinicLink link = existingLinkOpt.get();
                if ("ACTIVE".equalsIgnoreCase(link.getStatus())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("⚠️ Patient already linked to this clinic."); // 409 Conflict
                }
                // Reactivate previously unlinked record
                link.setStatus("ACTIVE");
                link.setLinkedAt(OffsetDateTime.now());
                // link.setUnlinkedAt(null); // if you track this
                patientClinicRepository.save(link);
                return ResponseEntity.ok("✅ Patient re-linked to clinic.");
            }

            //new link
            PatientClinicLink link = new PatientClinicLink();
            link.setPatientId(patientId);
            link.setClinicId(clinicId);
            link.setStatus("ACTIVE");
            link.setLinkedAt(OffsetDateTime.now());
            patientClinicRepository.save(link);

            return ResponseEntity.ok("✅ Patient successfully linked to clinic.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error linking patient: " + e.getMessage());
        }
    }

    //unlink patient
    public ResponseEntity<?> unlinkPatient(Long patientId, String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with email: " + email);
            }
            Long ownerUserId = userOpt.get().getUserId();

            Optional<Clinic> clinicOpt = clinicRepository.findByOwnerUserId(ownerUserId);
            if (clinicOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Clinic not found for user: " + email);
            }
            Long clinicId = clinicOpt.get().getClinicId();

            Optional<PatientClinicLink> existingLink =
                    patientClinicRepository.findByPatientIdAndClinicId(patientId, clinicId);

            if (existingLink.isEmpty()) {
                return ResponseEntity.status(404).body("No existing link found for this patient and clinic.");
            }

            PatientClinicLink link = existingLink.get();
            if ("INACTIVE".equalsIgnoreCase(link.getStatus())) {
                return ResponseEntity.ok("Link already inactive.");
            }

            link.setStatus("INACTIVE");
            patientClinicRepository.save(link);

            return ResponseEntity.ok("Patient successfully unlinked from clinic.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error unlinking patient: " + e.getMessage());
        }
    }

    //get all linked patients
    public List<LinkedPatientDTO> getLinkedPatients(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Clinic clinic = clinicRepository.findByOwnerUserId(user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No clinic found"));

        Long clinicId = clinic.getClinicId();

        // 1) Get active links
        List<PatientClinicLink> links = patientClinicRepository.findByClinicIdAndStatus(clinicId, "ACTIVE");

        // 2) For each link, load patient and map to DTO (use a PatientRepository or a JOIN in a custom query)
        return links.stream().map(link -> {
            Patient p = patientRepository.findById(link.getPatientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
            return new LinkedPatientDTO(
                    p.getPatientId(), p.getName(), p.getDob(), p.getGender(), p.getBloodGroup(),
                    p.getHeightCm(), p.getWeightKg(), p.getAllergies(), link.getLinkedAt()
            );
        }).toList();
    }

    public List<LinkedLabDTO> getLinkedLabs(Authentication auth) {
        Long clinicId = clinicRepository.findClinicIdByAdminEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("No clinic for admin"));
        List<Lab> labs = labRepository.findByLinkedClinicId(clinicId);
        return labs.stream()
                .map(l -> new LinkedLabDTO(
                        l.getLabId(),
                        l.getName(),
                        l.getEmail(),
                        l.getPhone(),
                        l.getLinkedClinicId()
                ))
                .toList();
    }

    public boolean unlinkDoctorFromClinic(Long doctorId, Authentication auth) {
        Long clinicId = clinicRepository.findClinicIdByAdminEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("No clinic for admin"));
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        if (!Objects.equals(d.getClinicId(), clinicId)) return false;
        d.setClinicId(null);
        doctorRepository.save(d);
        return true;
    }

    public boolean unlinkLabFromClinic(Long labId, Authentication auth) {
        Long clinicId = clinicRepository.findClinicIdByAdminEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("No clinic for admin"));
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new EntityNotFoundException("Lab not found"));
        if (!Objects.equals(lab.getLinkedClinicId(), clinicId)) return false;
        lab.setLinkedClinicId(null);
        labRepository.save(lab);
        return true;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // Get single clinic by ID - public / anyone
    public Clinic getClinicById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));
    }

    // Get all clinics
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    // Delete clinic
    public void deleteClinic(Long id) {
        if (!clinicRepository.existsById(id)) {
            throw new RuntimeException("Clinic not found");
        }
        clinicRepository.deleteById(id);
    }
}


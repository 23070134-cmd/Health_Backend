package com.health.lab.service;

import com.health.clinic.model.Clinic;
import com.health.clinic.repository.ClinicRepository;
import com.health.lab.dto.LabSearchDTO;
import com.health.lab.model.Lab;
import com.health.lab.repository.LabRepository;
import com.health.user.model.User;
import com.health.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LabService {

    @Autowired
    private final LabRepository labRepository;

    @Autowired
    private final ClinicRepository clinicRepository;

    @Autowired
    private final UserRepository userRepository;

    public LabService(LabRepository labRepository, ClinicRepository clinicRepository, UserRepository userRepository) {
        this.labRepository = labRepository;
        this.clinicRepository = clinicRepository;
        this.userRepository = userRepository;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // Register a new lab
    public ResponseEntity<?> registerLab(Lab lab, String username) {
        Optional<User> ownerOpt = userRepository.findByEmail(username);

        if (ownerOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Authenticated user not found"));
        }

        User owner = ownerOpt.get();
        lab.setOwnerUserId(owner.getUserId());
        lab.setCreatedAt(new Date());

        Lab saved = labRepository.save(lab);

        owner.setProfileCompleted(true);
        userRepository.save(owner);

        return ResponseEntity.ok(Map.of(
                "message", "Lab registered successfully",
                "labId", saved.getLabId(),
                "ownerUserId", owner.getUserId()
        ));
    }

    // Update lab details
    public ResponseEntity<?> updateLabDetails(Long id, Lab updatedLab) {
        try {
            Lab existingLab = labRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Lab not found with id: " + id));

            // Update only allowed fields
            if (updatedLab.getName() != null) existingLab.setName(updatedLab.getName());
            if (updatedLab.getEmail() != null) existingLab.setEmail(updatedLab.getEmail());
            if (updatedLab.getPhone() != null) existingLab.setPhone(updatedLab.getPhone());
            if (updatedLab.getAddress() != null) existingLab.setAddress(updatedLab.getAddress());

            labRepository.save(existingLab);

            return ResponseEntity.ok(Map.of(
                    "message", "Lab details updated successfully",
                    "labId", existingLab.getLabId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // directly link lab to clinic
    public ResponseEntity<?> linkLabToClinic(Long labId, Long clinicId, String username) {
        Optional<User> userOpt = userRepository.findByEmail(username);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Authenticated user not found"));
        }

        Optional<Lab> labOpt = labRepository.findById(labId);
        Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);

        if (labOpt.isEmpty() || clinicOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Lab or Clinic not found"));
        }

        Lab lab = labOpt.get();
        lab.setLinkedClinicId(clinicId);
        labRepository.save(lab);

        return ResponseEntity.ok(Map.of(
                "message", "Lab linked to clinic successfully",
                "labId", lab.getLabId(),
                "linkedClinicId", clinicId,
                "linkedBy", username
        ));
    }

    public ResponseEntity<?> getLabByOwner(Authentication authentication) {
        String email = authentication.getName();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        User user = userOpt.get();

        Optional<Lab> labOpt = labRepository.findByOwnerUserId(user.getUserId());
        if (labOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No lab found for this admin"));
        }

        return ResponseEntity.ok(labOpt.get());
    }

    public List<LabSearchDTO> searchLabs(String rawQuery) {
        String q = rawQuery == null ? "" : rawQuery.trim();
        if (q.length() < 2) return List.of();

        List<Lab> labs = labRepository.searchByNameOrOwnerEmail(q);

        return labs.stream()
                .map(l -> new LabSearchDTO(
                        l.getLabId(),
                        l.getName(),
                        l.getEmail(),
                        l.getPhone(),
                        l.getLinkedClinicId()
                ))
                .collect(Collectors.toList());
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // Get lab details by ID
    public ResponseEntity<?> getLabById(Long id) {
        Optional<Lab> labOpt = labRepository.findById(id);
        if (labOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Lab not found"));
        }
        return ResponseEntity.ok(labOpt.get());
    }
}



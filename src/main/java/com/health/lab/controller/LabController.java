package com.health.lab.controller;

import com.health.lab.dto.LabSearchDTO;
import com.health.lab.model.Lab;
import com.health.lab.service.LabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lab")
public class LabController {

    @Autowired
    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // register a lab
    @PostMapping("/register")
    public ResponseEntity<?> registerLab(@RequestBody Lab lab, Authentication authentication) {
        try {
            // Extract owner user ID from authentication principal
            String username = authentication.getName();
            return labService.registerLab(lab, username);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update lab details
    @PreAuthorize("hasAnyRole('LAB_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLabDetails(@PathVariable Long id, @RequestBody Lab updatedLab) {
        try {
            return labService.updateLabDetails(id, updatedLab);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // get me detail
    @GetMapping("/me")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<?> getMyLab(Authentication authentication) {
        return labService.getLabByOwner(authentication);
    }

    // link lab to clinic
    @PreAuthorize("hasAnyRole('LAB_ADMIN', 'SUPER_ADMIN', 'CLINIC_ADMIN')")
    @PostMapping("/{id}/linkClinic/{clinicId}")
    public ResponseEntity<?> linkClinic(@PathVariable Long id, @PathVariable Long clinicId, Authentication authentication) {
        try {
            // Extract username (email) from authentication principal
            String username = authentication.getName();
            return labService.linkLabToClinic(id, clinicId, username);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //search for specific lab
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<LabSearchDTO>> searchLabs(@RequestParam("query") String query) {
        List<LabSearchDTO> results = labService.searchLabs(query);
        return ResponseEntity.ok(results);
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//

    // get lab by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getLabById(@PathVariable Long id) {
        return labService.getLabById(id);
    }
}

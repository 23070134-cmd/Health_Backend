package com.health.report.controller;

import com.health.report.model.LabReport;
import com.health.report.service.LabReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/report")
public class LabReportController {

    @Autowired
    private final LabReportService labReportService;

    public LabReportController(LabReportService labReportService) {
        this.labReportService = labReportService;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // Upload Report
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReport(@RequestBody LabReport report) {
        return labReportService.uploadReport(report);
    }

    // 2 View all reports for patient
    @GetMapping("/view/{patientId}")
    public ResponseEntity<?> viewReports(@PathVariable Long patientId) {
        return labReportService.getReportsByPatient(patientId);
    }

    // 3 Share report with other clinics
    @PostMapping("/share/{targetClinicId}")
    public ResponseEntity<?> shareReport(
            @PathVariable Long targetClinicId,
            @RequestParam Long reportId
    ) {
        return labReportService.shareReport(reportId, targetClinicId);
    }

    // 4 Get summarized/common reports
    @GetMapping("/common/{patientId}")
    public ResponseEntity<?> getCommonReports(@PathVariable Long patientId) {
        return labReportService.getCommonReports(patientId);
    }

    // 5 Delete report
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        return labReportService.deleteReport(reportId);
    }

    //get all report for a specific lab
    @GetMapping("/lab/{labId}")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<?> getReportsByLab(@PathVariable Long labId) {
        return ResponseEntity.ok(labReportService.getReportsByLab(labId));
    }

    //get report for a particular clinic
    @GetMapping("/clinic/{clinicId}")
    @PreAuthorize("hasAnyRole('CLINIC_ADMIN', 'SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<?> getReportsByClinic(@PathVariable Long clinicId) {
        return ResponseEntity.ok(labReportService.getReportsByClinic(clinicId));
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//
}

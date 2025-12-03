package com.health.report.service;

import com.health.report.model.LabReport;
import com.health.report.repository.LabReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LabReportService {

    @Autowired
    private final LabReportRepository labReportRepository;

    public LabReportService(LabReportRepository labReportRepository) {
        this.labReportRepository = labReportRepository;
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------USED--------------------------------------------//
    //----------------------------------------------------------------------------------//

    // Upload Report
    public ResponseEntity<?> uploadReport(LabReport report) {
        report.setUploadedAt(java.time.Instant.now());
        labReportRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Report uploaded successfully!"));
    }

    // View all reports for patient
    public ResponseEntity<?> getReportsByPatient(Long patientId) {
        List<LabReport> reports = labReportRepository.findByPatientId(patientId);
        return ResponseEntity.ok(reports);
    }

    //share report
    public ResponseEntity<?> shareReport(Long reportId, Long targetClinicId) {
        Optional<LabReport> existing = labReportRepository.findById(reportId);
        if (existing.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Report not found"));
        }

        LabReport report = existing.get();
        LabReport sharedCopy = new LabReport();
        sharedCopy.setClinicId(targetClinicId);
        sharedCopy.setLabId(report.getLabId());
        sharedCopy.setPatientId(report.getPatientId());
        sharedCopy.setTestName(report.getTestName());
        sharedCopy.setReportUrl(report.getReportUrl());
        sharedCopy.setResultSummary(report.getResultSummary());
        sharedCopy.setUploadedAt(java.time.Instant.now());
        labReportRepository.save(sharedCopy);

        return ResponseEntity.ok(Map.of(
                "message", "Report shared successfully!",
                "sharedReportId", sharedCopy.getReportId()
        ));
    }


    // Get common summary (aggregated reports)
    public ResponseEntity<?> getCommonReports(Long patientId) {
        List<LabReport> reports = labReportRepository.findByPatientId(patientId);
        if (reports.isEmpty()) {
            return ResponseEntity.ok("No reports available for patient");
        }

        String summary = "Total reports: " + reports.size();
        return ResponseEntity.ok(summary);
    }

    // Delete report
    public ResponseEntity<?> deleteReport(Long reportId) {
        if (!labReportRepository.existsById(reportId)) {
            return ResponseEntity.badRequest().body("Report not found");
        }
        labReportRepository.deleteById(reportId);
        return ResponseEntity.ok("Report deleted successfully!");
    }

    //get reports for a lab
    public List<LabReport> getReportsByLab(Long labId) {
        return labReportRepository.findByLabId(labId);
    }

    // reports for a specifc clinic
    public List<LabReport> getReportsByClinic(Long clinicId) {
        return labReportRepository.findByClinicId(clinicId);
    }

    //------------------------------------------------------------------------------------//
    //-----------------------------------NOT USED----------------------------------------//
    //----------------------------------------------------------------------------------//
}


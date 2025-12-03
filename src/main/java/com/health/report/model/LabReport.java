package com.health.report.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lab_reports")
public class LabReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "lab_id")
    private Long labId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "clinic_id")
    private Long clinicId;

    @Column(name = "test_name")
    private String testName;

    @Column(name = "report_url")
    private String reportUrl;

    @Column(name = "result_summary")
    private String resultSummary;

    @Column(name = "uploaded_at", columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private Instant uploadedAt = Instant.now();

    // Getters and Setters
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getLabId() { return labId; }
    public void setLabId(Long labId) { this.labId = labId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }

    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }

    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}

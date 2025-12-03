package com.health.patient.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "patient_clinic_links")
public class PatientClinicLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "clinic_id")
    private Long clinicId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "linked_at", columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private OffsetDateTime linkedAt;

    // Getters and Setters
    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(OffsetDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }
}


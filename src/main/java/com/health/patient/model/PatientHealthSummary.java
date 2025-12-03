package com.health.patient.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_health_summary")
public class PatientHealthSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "sugar_level")
    private String sugarLevel;

    @Column(name = "cholesterol_level")
    private String cholesterolLevel;

    @Column(name = "heart_rate")
    private String heartRate;

    @Column(name = "existing_conditions")
    private String existingConditions;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // --- Manual Getters and Setters ---
    public Long getSummaryId() { return summaryId; }
    public void setSummaryId(Long summaryId) { this.summaryId = summaryId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    public String getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(String sugarLevel) { this.sugarLevel = sugarLevel; }

    public String getCholesterolLevel() { return cholesterolLevel; }
    public void setCholesterolLevel(String cholesterolLevel) { this.cholesterolLevel = cholesterolLevel; }

    public String getHeartRate() { return heartRate; }
    public void setHeartRate(String heartRate) { this.heartRate = heartRate; }

    public String getExistingConditions() { return existingConditions; }
    public void setExistingConditions(String existingConditions) { this.existingConditions = existingConditions; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}


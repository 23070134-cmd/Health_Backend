package com.health.doctor.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public class DoctorResponseDTO {
    private Long doctorId;
    private String name;
    private String specialization;
    private String licenseNumber;
    private Integer experienceYears;
    private String gender;
    private JsonNode availableDays;
    private LocalDateTime createdAt;

    // Getters & Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public JsonNode getAvailableDays() { return availableDays; }
    public void setAvailableDays(JsonNode availableDays) { this.availableDays = availableDays; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

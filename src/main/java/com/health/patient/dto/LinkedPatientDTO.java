package com.health.patient.dto;

// DTO with aligned types
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LinkedPatientDTO(
        Long patientId,
        String name,
        LocalDate dob,           // LocalDate instead of String
        String gender,
        String bloodGroup,
        Double heightCm,         // Double instead of Integer
        Double weightKg,         // Double instead of Integer
        String allergies,
        OffsetDateTime linkedAt
) {}


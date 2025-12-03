package com.health.lab.dto;

public record LinkedLabDTO(
        Long labId,
        String name,
        String email,
        String phone,
        Long linkedClinicId
) {}

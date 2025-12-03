package com.health.doctor.mapper;

import com.health.doctor.dto.DoctorResponseDTO;
import com.health.doctor.model.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DoctorMapper {

    public DoctorResponseDTO toResponse(Doctor doctor) {
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setDoctorId(doctor.getDoctorId());
        dto.setName(doctor.getName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setGender(doctor.getGender());
        dto.setAvailableDays(doctor.getAvailableDays());
        dto.setCreatedAt(doctor.getCreatedAt());
        return dto;
    }

    public List<DoctorResponseDTO> toResponseList(List<Doctor> doctors) {
        return doctors.stream().map(this::toResponse).collect(Collectors.toList());
    }
}

package com.health.clinic.mapper;

import com.health.clinic.dto.ClinicRequestDTO;
import com.health.clinic.dto.ClinicResponseDTO;
import com.health.clinic.model.Clinic;
import org.springframework.stereotype.Component;

@Component
public class ClinicMapper {
    public ClinicResponseDTO getClinicResponseDTO(Clinic savedClinic) {
        ClinicResponseDTO response = new ClinicResponseDTO();
        response.setClinicId(savedClinic.getClinicId());
        response.setName(savedClinic.getName());
        response.setEmail(savedClinic.getEmail());
        response.setPhone(savedClinic.getPhone());
        response.setAddress(savedClinic.getAddress());
        response.setCity(savedClinic.getCity());
        response.setState(savedClinic.getState());
        response.setPincode(savedClinic.getPincode());
        response.setOwnerUserId(savedClinic.getOwnerUserId());
        response.setCreatedAt(savedClinic.getCreatedAt());
        response.setUpdatedAt(savedClinic.getUpdatedAt());
        return response;
    }

    public Clinic getClinicEntity(ClinicRequestDTO dto){
        Clinic clinic = new Clinic();
        clinic.setName(dto.getName());
        clinic.setEmail(dto.getEmail());
        clinic.setPhone(dto.getPhone());
        clinic.setAddress(dto.getAddress());
        clinic.setCity(dto.getCity());
        clinic.setState(dto.getState());
        clinic.setPincode(dto.getPincode());
        return clinic;
    }
}

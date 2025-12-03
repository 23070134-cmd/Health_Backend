// src/main/java/com/health/lab/dto/LabSearchDTO.java
package com.health.lab.dto;

public class LabSearchDTO {
    private Long labId;
    private String name;
    private String email;
    private String phone;
    private Long linkedClinicId;

    public LabSearchDTO() {}

    public LabSearchDTO(Long labId, String name, String email, String phone, Long linkedClinicId) {
        this.labId = labId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.linkedClinicId = linkedClinicId;
    }

    public Long getLabId() { return labId; }
    public void setLabId(Long labId) { this.labId = labId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getLinkedClinicId() { return linkedClinicId; }
    public void setLinkedClinicId(Long linkedClinicId) { this.linkedClinicId = linkedClinicId; }
}

package com.health.lab.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Table(name = "lab")
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labId;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Foreign keys
    private Long linkedClinicId;
    private Long ownerUserId;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Getters and Setters
    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getLinkedClinicId() {
        return linkedClinicId;
    }

    public void setLinkedClinicId(Long linkedClinicId) {
        this.linkedClinicId = linkedClinicId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        if (createdAt != null) {
            this.createdAt = createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC);
        }
    }
}

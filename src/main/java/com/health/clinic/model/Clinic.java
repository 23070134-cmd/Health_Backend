package com.health.clinic.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "clinic")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clinicId;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;

    @Column(name = "owner_user_id")
    private Long ownerUserId; // manual mapping, not handled by JPA

    @Column(columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private Instant createdAt = Instant.now();

    @Column(columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private Instant updatedAt = Instant.now();

    public Clinic() {
    }

    // Getters and Setters
    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

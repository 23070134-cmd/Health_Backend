package com.health.doctor.repository;

import com.health.doctor.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findAllByClinicId(Long clinicId);
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findByNameContainingIgnoreCase(String name);
}


// com.health.doctor.repository.DoctorPatientRepository.java
package com.health.doctor.repository;

import com.health.doctor.model.DoctorPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorPatientRepository extends JpaRepository<DoctorPatient, Long> {

    // Get all patients for a doctor
    List<DoctorPatient> findByDoctorId(Long doctorId);

    // Get patients by status
    List<DoctorPatient> findByDoctorIdAndStatus(Long doctorId, String status);

    // Check if relationship exists
    Optional<DoctorPatient> findByDoctorIdAndPatientId(Long doctorId, Long patientId);
}

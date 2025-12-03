package com.health.patient.repository;

import com.health.patient.model.Patient;
import com.health.patient.model.PatientClinicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Get a patient by user account
    Optional<Patient> findByUserId(Long userId);

    // Global patient search (used by /patient/search?query=)
    List<Patient> findByNameContainingIgnoreCase(String name);

    // Search patients by clinic (IDs) + optional name filter
    List<Patient> findByPatientIdInAndNameContainingIgnoreCase(List<Long> ids, String name);

    // Get all patients by a list of IDs (used in /patient/search/{clinicId})
    List<Patient> findByPatientIdIn(List<Long> ids);

}

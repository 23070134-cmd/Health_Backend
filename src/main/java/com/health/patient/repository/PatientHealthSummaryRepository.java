package com.health.patient.repository;

import com.health.patient.model.PatientHealthSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientHealthSummaryRepository extends JpaRepository<PatientHealthSummary, Long> {
    Optional<PatientHealthSummary> findByPatientId(Long patientId);
}


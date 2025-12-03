package com.health.patient.repository;

import com.health.patient.model.PatientClinicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientClinicRepository extends JpaRepository<PatientClinicLink, Long> {

    Optional<PatientClinicLink> findByPatientIdAndClinicId(Long patientId, Long clinicId);

    // fetch all clinics a patient has ever been linked to
    List<PatientClinicLink> findByPatientId(Long patientId);

    // fetch all patients linked to a specific clinic
    List<PatientClinicLink> findByClinicId(Long clinicId);

    List<PatientClinicLink> findByClinicIdAndStatus(Long clinicId, String status);
}

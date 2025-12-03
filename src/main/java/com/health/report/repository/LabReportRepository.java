package com.health.report.repository;

import com.health.report.model.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientIdAndClinicId(Long patientId, Long clinicId);
    List<LabReport> findByLabId(Long labId);
    List<LabReport> findByPatientId(Long patientId);
    List<LabReport> findByClinicId(Long clinicId);

}

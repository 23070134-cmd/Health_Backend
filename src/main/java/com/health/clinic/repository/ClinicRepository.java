// com.health.clinic.repository.ClinicRepository
package com.health.clinic.repository;

import com.health.clinic.model.Clinic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Optional<Clinic> findByOwnerUserId(Long ownerUserId);

    // Map admin email -> clinicId using a join with users table
    @Query("""
        select c.clinicId
        from Clinic c
        join com.health.user.model.User u on u.userId = c.ownerUserId
        where lower(u.email) = lower(?1)
    """)
    Optional<Long> findClinicIdByAdminEmail(String adminEmail);
}

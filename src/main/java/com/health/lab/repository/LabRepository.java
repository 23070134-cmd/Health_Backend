// com.health.lab.repository.LabRepository
package com.health.lab.repository;

import com.health.lab.model.Lab;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LabRepository extends JpaRepository<Lab, Long> {
    boolean existsByEmail(String email);
    List<Lab> findByLinkedClinicId(Long clinicId);
    Optional<Lab> findByOwnerUserId(Long ownerUserId);

    @Query("""
        select l from Lab l
        join com.health.user.model.User u on u.userId = l.ownerUserId
        where lower(l.name) like lower(concat('%', :q, '%'))
           or lower(u.email) like lower(concat('%', :q, '%'))
    """)
    List<Lab> searchByNameOrOwnerEmail(@Param("q") String q);
}

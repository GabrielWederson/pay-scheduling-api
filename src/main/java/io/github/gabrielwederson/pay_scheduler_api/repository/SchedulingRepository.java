package io.github.gabrielwederson.pay_scheduler_api.repository;

import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {

    @Query("SELECT s FROM Scheduling s WHERE s.id = :id AND s.status = :status")
    Optional<Scheduling> findByStatusAndId( @Param("id") Long id,
            @Param("status") Status status
    );
}

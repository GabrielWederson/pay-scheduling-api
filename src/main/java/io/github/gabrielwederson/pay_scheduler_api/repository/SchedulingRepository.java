package io.github.gabrielwederson.pay_scheduler_api.repository;

import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {

}

package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {

    @Autowired
    SchedulingRepository repository;


}

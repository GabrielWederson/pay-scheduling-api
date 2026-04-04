package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scheduling/v1")
public class SchedulingController {

    @Autowired
    SchedulingService schedulingService;

    @PostMapping
    public SchedulingResponseDTO createScheduling(@RequestBody SchedulingRequestDTO schedulingRequestDTO){
        return schedulingService.create(schedulingRequestDTO);
    }
}

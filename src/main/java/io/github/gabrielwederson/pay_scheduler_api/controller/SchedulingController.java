package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling/v1")
public class SchedulingController {

    @Autowired
    SchedulingService schedulingService;

    @PostMapping
    public SchedulingResponseDTO createScheduling(@RequestBody SchedulingRequestDTO schedulingRequestDTO){
        return schedulingService.create(schedulingRequestDTO);
    }

    @GetMapping
    public List<SchedulingResponseDTO> findAllAppointments(){
        return schedulingService.findAll();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteScheduling(@PathVariable("id") Long id){
        schedulingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

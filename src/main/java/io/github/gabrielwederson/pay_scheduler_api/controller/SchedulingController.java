package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.controller.docs.SchedulingControllerDocs;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.service.SchedulingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling/v1")
@Tag(name = "Appointments", description = "endpoints to management appointments")
public class SchedulingController implements SchedulingControllerDocs {

    @Autowired
    SchedulingService schedulingService;

    @PostMapping
    @Override
    public SchedulingResponseDTO createScheduling(@RequestBody SchedulingRequestDTO schedulingRequestDTO){
        return schedulingService.create(schedulingRequestDTO);
    }

    @GetMapping
    @Override
    public List<SchedulingResponseDTO> findAllAppointments(){
        return schedulingService.findAll();
    }

    @DeleteMapping(value = "/{id}")
    @Override
    public ResponseEntity<?> deleteScheduling(@PathVariable("id") Long id){
        schedulingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/today")
    @Override
    public List<SchedulingResponseDTO> findTodayAppointments(){
        return schedulingService.findByStatusAndDateNow();
    }
}

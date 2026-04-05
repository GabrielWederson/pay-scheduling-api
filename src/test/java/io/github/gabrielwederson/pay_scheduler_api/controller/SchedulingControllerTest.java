package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidDataException;
import io.github.gabrielwederson.pay_scheduler_api.exception.SchedulingNotFound;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.service.SchedulingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulingControllerTest {

    @Mock
    private SchedulingService schedulingService;

    @InjectMocks
    private SchedulingController schedulingController;

    private SchedulingRequestDTO requestDTO;
    private SchedulingResponseDTO responseDTO;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        futureDate = LocalDateTime.now().plusDays(1);

        requestDTO = new SchedulingRequestDTO();
        requestDTO.setOriginAccount("123");
        requestDTO.setDestinationAccount("321");
        requestDTO.setValue(new BigDecimal("100.00"));
        requestDTO.setSchedulingDate(futureDate);

        responseDTO = new SchedulingResponseDTO();
        responseDTO.setOriginAccount("123");
        responseDTO.setDestinationAccount("321");
        responseDTO.setValue(new BigDecimal("100.00"));
        responseDTO.setStatus(Status.PENDING);
        responseDTO.setSchedulingDate(futureDate);
        responseDTO.setCreated_at(LocalDateTime.now());
    }

    @Test
    void ShouldReturnCreatedScheduling() {
        // given
        when(schedulingService.create(any(SchedulingRequestDTO.class))).thenReturn(responseDTO);

        // when
        SchedulingResponseDTO result = schedulingController.createScheduling(requestDTO);

        // then
        assertNotNull(result);
        assertEquals("123", result.getOriginAccount());
        assertEquals("321", result.getDestinationAccount());
        assertEquals(new BigDecimal("100.00"), result.getValue());
        assertEquals(Status.PENDING, result.getStatus());
        verify(schedulingService, times(1)).create(any(SchedulingRequestDTO.class));
    }

    @Test
    void ShouldThrowException_WhenInsufficientBalance() {
        // given
        when(schedulingService.create(any(SchedulingRequestDTO.class)))
                .thenThrow(new InvalidDataException("Insufficient balance for this scheduling"));

        // when adn Then
        assertThrows(InvalidDataException.class, () -> {
            schedulingController.createScheduling(requestDTO);
        });
        verify(schedulingService, times(1)).create(any(SchedulingRequestDTO.class));
    }

    @Test
    void ShouldThrowException_WhenAccountNotFound() {
        // given
        when(schedulingService.create(any(SchedulingRequestDTO.class)))
                .thenThrow(new AccountNotFound("Account with this origin number not found"));

        // when and then
        assertThrows(AccountNotFound.class, () -> {
            schedulingController.createScheduling(requestDTO);
        });
        verify(schedulingService, times(1)).create(any(SchedulingRequestDTO.class));
    }

    @Test
    void ShouldThrowException_WhenDateIsInPast() {
        // given
        requestDTO.setSchedulingDate(LocalDateTime.now().minusDays(1));
        when(schedulingService.create(any(SchedulingRequestDTO.class)))
                .thenThrow(new InvalidDataException("The appointment date cannot be before or the same as today"));

        // when and then
        assertThrows(InvalidDataException.class, () -> {
            schedulingController.createScheduling(requestDTO);
        });
        verify(schedulingService, times(1)).create(any(SchedulingRequestDTO.class));
    }

    @Test
    void ShouldReturnListOfScheduling() {
        // given
        SchedulingResponseDTO responseDTO2 = new SchedulingResponseDTO();
        responseDTO2.setOriginAccount("456");
        responseDTO2.setDestinationAccount("654");
        responseDTO2.setValue(new BigDecimal("200.00"));
        responseDTO2.setStatus(Status.PENDING);

        List<SchedulingResponseDTO> schedulings = Arrays.asList(responseDTO, responseDTO2);
        when(schedulingService.findAll()).thenReturn(schedulings);

        // when
        List<SchedulingResponseDTO> result = schedulingController.findAllAppointments();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("123", result.get(0).getOriginAccount());
        assertEquals("456", result.get(1).getOriginAccount());
        verify(schedulingService, times(1)).findAll();
    }

    @Test
    void ShouldReturnEmptyList_WhenNoScheduling() {
        // given
        when(schedulingService.findAll()).thenReturn(Arrays.asList());

        // when
        List<SchedulingResponseDTO> result = schedulingController.findAllAppointments();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schedulingService, times(1)).findAll();
    }

    @Test
    void ShouldReturnNoContent_WhenValidId() {
        // given
        Long id = 1L;
        doNothing().when(schedulingService).delete(id);

        // when
        ResponseEntity<?> response = schedulingController.deleteScheduling(id);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(schedulingService, times(1)).delete(id);
    }

    @Test
    void ShouldThrowException_WhenSchedulingNotFound() {
        // given
        Long id = 999L;
        doThrow(new SchedulingNotFound("Scheduling not found or not in PENDING status"))
                .when(schedulingService).delete(id);

        // when and then
        assertThrows(SchedulingNotFound.class, () -> {
            schedulingController.deleteScheduling(id);
        });
        verify(schedulingService, times(1)).delete(id);
    }

    @Test
    void ShouldReturnListOfTodayScheduling() {
        // fiven
        List<SchedulingResponseDTO> todaySchedulings = Arrays.asList(responseDTO);
        when(schedulingService.findByStatusAndDateNow()).thenReturn(todaySchedulings);

        // when
        List<SchedulingResponseDTO> result = schedulingController.findTodayAppointments();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.PENDING, result.get(0).getStatus());
        verify(schedulingService, times(1)).findByStatusAndDateNow();
    }

    @Test
    void ShouldReturnEmptyList_WhenNoTodayScheduling() {
        // given
        when(schedulingService.findByStatusAndDateNow()).thenReturn(Arrays.asList());

        // when
        List<SchedulingResponseDTO> result = schedulingController.findTodayAppointments();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schedulingService, times(1)).findByStatusAndDateNow();
    }
}

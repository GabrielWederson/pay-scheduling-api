package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import static io.github.gabrielwederson.pay_scheduler_api.mapper.ObjectMapper.parseObjectMapper;

import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    AccountRepository repository;

    @Mock
    SchedulingRepository schedulingRepository;

    @InjectMocks
    SchedulingService service;

    SchedulingRequestDTO scheduling;

    @BeforeEach
    void setup(){
    scheduling = new SchedulingRequestDTO("123", "456", new BigDecimal("20.0"), LocalDateTime.now().plusDays(1));

    }

    @Test
    void shouldCreateScheduling() {
        //when
        when(repository.findAccountByNumber("123")).thenReturn(Optional.of(new Account()));
        when(repository.findAccountByNumber("456")).thenReturn(Optional.of(new Account()));
        when(schedulingRepository.save(any(Scheduling.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //then
        var result = service.createSchedule(scheduling);
        //assertions
        assertNotNull(result);
        assertEquals("123", scheduling.getOriginAccount());
        assertEquals("456", scheduling.getDestinationAccount());
        assertNotEquals(scheduling.getValue(), 0);
        assertNotEquals(scheduling.getSchedulingDate(), LocalDateTime.now());
    }
}
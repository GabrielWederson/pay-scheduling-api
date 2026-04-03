package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidDataException;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.github.gabrielwederson.pay_scheduler_api.mapper.ObjectMapper.parseObjectMapper;

@Service
public class SchedulingService {

    private Logger logger = LoggerFactory.getLogger(SchedulingService.class.getName());

    @Autowired
    SchedulingRepository schedulingRepository;

    @Autowired
    AccountRepository accountRepository;

    public SchedulingRequestDTO createSchedule(SchedulingRequestDTO requestDTO){
        logger.info("creating a scheduling");

        Account origin = accountRepository
                .findAccountByNumber(requestDTO.getOriginAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this origin number not found"));

        Account destination = accountRepository
                .findAccountByNumber(requestDTO.getDestinationAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this destination number not found"));

        if(requestDTO.getSchedulingDate() == null || !requestDTO.getSchedulingDate().isAfter(LocalDateTime.now()))
            throw new InvalidDataException("The appointment date cannot be before or the same as today");

        if (requestDTO.getValue() == null || requestDTO.getValue().compareTo(BigDecimal.ONE) < 0 || requestDTO.getValue().compareTo(new BigDecimal("5000")) > 0)
            throw new InvalidDataException("The value should be between $1 and $5000.");


        var entity = parseObjectMapper(requestDTO, Scheduling.class);

        entity.setCreated_at(LocalDateTime.now());
        entity.setStatus(Status.PENDING);

        var saved = schedulingRepository.save(entity);

        return parseObjectMapper(saved, SchedulingRequestDTO.class);
        }
}

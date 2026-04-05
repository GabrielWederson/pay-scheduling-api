package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidDataException;
import io.github.gabrielwederson.pay_scheduler_api.exception.SchedulingNotFound;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.gabrielwederson.pay_scheduler_api.mapper.ObjectMapper.parseObjectMapper;
import static io.github.gabrielwederson.pay_scheduler_api.mapper.ObjectMapper.parseListObjectMapper;

@Service
public class SchedulingService {

    private Logger logger = LoggerFactory.getLogger(SchedulingService.class.getName());

    @Autowired
    private SchedulingRepository schedulingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SchedulingSchedulerService schedulerService;

    @Transactional
    public SchedulingResponseDTO create(SchedulingRequestDTO requestDTO) {
        logger.info("Creating a scheduling");

        Account origin = accountRepository
                .findAccountByNumber(requestDTO.getOriginAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this origin number not found"));

        Account destination = accountRepository
                .findAccountByNumber(requestDTO.getDestinationAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this destination number not found"));

        if(requestDTO.getValue().compareTo(origin.getBalance()) > 0) throw new InvalidDataException("Insufficient balance for this scheduling");

        if (requestDTO.getSchedulingDate() == null || !requestDTO.getSchedulingDate().isAfter(LocalDateTime.now()))
            throw new InvalidDataException("The appointment date cannot be before or the same as today");

        if (requestDTO.getValue() == null || requestDTO.getValue().compareTo(BigDecimal.ONE) < 0 || requestDTO.getValue().compareTo(new BigDecimal("5000")) > 0)
            throw new InvalidDataException("The value should be between $1 and $5000.");

        Scheduling entity = parseObjectMapper(requestDTO, Scheduling.class);

        entity.setCreated_at(LocalDateTime.now());
        entity.setStatus(Status.PENDING);
        entity.setOriginAccount(requestDTO.getOriginAccount());
        entity.setDestinationAccount(requestDTO.getDestinationAccount());

        Scheduling saved = schedulingRepository.save(entity);


        schedulerService.scheduleVerification(saved);

        return parseObjectMapper(saved, SchedulingResponseDTO.class);
    }

    public List<SchedulingResponseDTO> findAll() {
        logger.info("Listing appointments");
        return parseListObjectMapper(schedulingRepository.findAll(), SchedulingResponseDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting Scheduling");
        Scheduling entity = schedulingRepository.findByStatusAndId(id, Status.PENDING)
                .orElseThrow(() -> new SchedulingNotFound("Scheduling not found or not in PENDING status"));

        schedulerService.cancelScheduledJob(id);
        schedulingRepository.delete(entity);
    }

    public List<SchedulingResponseDTO> findByStatusAndDateNow(){
        LocalDate today = LocalDate.now();

        List<Scheduling> entities = schedulingRepository.findByStatusAndDate(Status.PENDING, today);

        return parseListObjectMapper(entities, SchedulingResponseDTO.class);

    }
}

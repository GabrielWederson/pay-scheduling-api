package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.SchedulingResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidDataException;
import io.github.gabrielwederson.pay_scheduler_api.exception.SchedulingNotFoundException;
import io.github.gabrielwederson.pay_scheduler_api.exception.UserNotFoundException;
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

@Service
public class SchedulingService {

    private Logger logger = LoggerFactory.getLogger(SchedulingService.class.getName());

    @Autowired
    private SchedulingRepository schedulingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SchedulingSchedulerService schedulerService;

    @Autowired
    private EmailService emailService;

    private static final String subject = "NEW SCHEDULED PAYMENT";

    private static final String body = "you have a new payment scheduled";

    @Transactional
    public SchedulingResponseDTO create(SchedulingRequestDTO requestDTO) {
        logger.info("Creating a scheduling with fund reservation");


        Account origin = accountRepository
                .findAccountByNumberWithLock(requestDTO.getOriginAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this origin number not found"));

        Account destination = accountRepository
                .findAccountByNumber(requestDTO.getDestinationAccount())
                .orElseThrow(() -> new AccountNotFound("Account with this destination number not found"));

        BigDecimal availableBalance = origin.getAvailableBalance();
        if (requestDTO.getValue().compareTo(availableBalance) > 0) {
            throw new InvalidDataException("Insufficient balance for this scheduling. Available: " + availableBalance);
        }

        if (requestDTO.getSchedulingDate() == null || !requestDTO.getSchedulingDate().isAfter(LocalDateTime.now()))
            throw new InvalidDataException("The appointment date cannot be before or the same as today");

        if (requestDTO.getValue() == null || requestDTO.getValue().compareTo(BigDecimal.ONE) < 0 || requestDTO.getValue().compareTo(new BigDecimal("5000")) > 0)
            throw new InvalidDataException("The value should be between $1 and $5000.");


        origin.setReservedBalance(origin.getReservedBalance().add(requestDTO.getValue()));
        accountRepository.save(origin);


        Scheduling entity = new Scheduling();
        entity.setOriginAccount(origin);
        entity.setDestinationAccount(destination);
        entity.setValue(requestDTO.getValue());
        entity.setSchedulingDate(requestDTO.getSchedulingDate());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus(Status.PENDING);

        Scheduling saved = schedulingRepository.save(entity);
        schedulerService.scheduleVerification(saved);

        String date = requestDTO.getSchedulingDate().toString();
        String amount = requestDTO.getValue().toString();
        String emailBody = body + " reserved on the date: " + date + " with the amount of: " + amount;

        String originEmail = accountRepository
                .findUserEmailByAccountNumber(requestDTO.getOriginAccount())
                .orElseThrow(() -> new UserNotFoundException("User of origin payment email not found"));

        String destinationEmail = accountRepository
                .findUserEmailByAccountNumber(requestDTO.getDestinationAccount())
                .orElseThrow(() -> new UserNotFoundException("User of destination payment email not found"));

        emailService.sendEmail(originEmail, subject, emailBody);
        emailService.sendEmail(destinationEmail, subject, emailBody);

        return new SchedulingResponseDTO(saved);
    }

    public List<SchedulingResponseDTO> findAll() {
        logger.info("Listing appointments");
        return schedulingRepository.findAll().stream()
                .map(SchedulingResponseDTO::new)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting Scheduling");
        Scheduling entity = schedulingRepository.findByStatusAndId(id, Status.PENDING)
                .orElseThrow(() -> new SchedulingNotFoundException("Scheduling not found or not in PENDING status"));

        schedulerService.cancelScheduledJob(id);
        schedulingRepository.delete(entity);
    }

    public List<SchedulingResponseDTO> findByStatusAndDateNow(){
        LocalDate today = LocalDate.now();
        List<Scheduling> entities = schedulingRepository.findByStatusAndDate(Status.PENDING, today);
        return entities.stream()
                .map(SchedulingResponseDTO::new)
                .toList();

    }
}

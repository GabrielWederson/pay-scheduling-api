package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.UserNotFound;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulingSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final SchedulingRepository schedulingRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final Map<Long, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private static final String subject = "NEWS ABOUT SCHEDULED PAYMENT";
    private static final String body = "Your scheduled payment";

    public SchedulingSchedulerService(TaskScheduler taskScheduler,
                                      SchedulingRepository schedulingRepository,
                                      AccountRepository accountRepository, EmailService emailService) {
        this.taskScheduler = taskScheduler;
        this.schedulingRepository = schedulingRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
    }

    public void scheduleVerification(Scheduling scheduling) {
        LocalDateTime executionTime = scheduling.getSchedulingDate();
        LocalDateTime verificationTime = executionTime.minusMinutes(1);

        if (verificationTime.isBefore(LocalDateTime.now())) {
            executeVerification(scheduling);
            return;
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeVerification(scheduling),
                Date.from(verificationTime.atZone(ZoneId.systemDefault()).toInstant())
        );

        scheduledJobs.put(scheduling.getId(), future);
        logger.info("Scheduled verification");
    }

    @Transactional
    private void executeVerification(Scheduling scheduling) {
        logger.info("Executing verification");

        try {
            Account origin = accountRepository
                    .findAccountByNumber(scheduling.getOriginAccount())
                    .orElseThrow(() -> new AccountNotFound("This account doesn't exist"));

            if (origin.getBalance().compareTo(scheduling.getValue()) >= 0) {
                scheduleExactPayment(scheduling);
            } else {
                scheduling.setStatus(Status.FAILED);
                schedulingRepository.save(scheduling);
                logger.warn("Insufficient balance");
                String date = scheduling.getSchedulingDate().toString();
                String originEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getOriginAccount())
                        .orElseThrow(() -> new UserNotFound("User of origin payment email not found"));

                String destinationEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getDestinationAccount())
                        .orElseThrow(() -> new UserNotFound("User of destination payment email not found"));
                emailService.sendEmail(originEmail, subject, body + " was failed!!, on the date: " + date);
                emailService.sendEmail(destinationEmail, subject, body + " was failed!!, on the date: " + date);

            }
        } catch (Exception e) {
            logger.error("Error during verification");
        }
    }

    private void scheduleExactPayment(Scheduling scheduling) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executePayment(scheduling),
                Date.from(scheduling.getSchedulingDate().atZone(ZoneId.systemDefault()).toInstant())
        );

        scheduledJobs.put(scheduling.getId(), future);
        logger.info("Scheduled exact payment");
    }

    @Transactional
    public void executePayment(Scheduling scheduling) {
        logger.info("Executing payment");

        try {
            Account origin = accountRepository
                    .findAccountByNumber(scheduling.getOriginAccount())
                    .orElseThrow(() -> new AccountNotFound("This account doesn't exist"));

            Account destination = accountRepository
                    .findAccountByNumber(scheduling.getDestinationAccount())
                    .orElseThrow(() -> new AccountNotFound("Destination account doesn't exist"));

            if (origin.getBalance().compareTo(scheduling.getValue()) >= 0) {
                origin.setBalance(origin.getBalance().subtract(scheduling.getValue()));
                accountRepository.save(origin);

                destination.setBalance(destination.getBalance().add(scheduling.getValue()));
                accountRepository.save(destination);

                scheduling.setStatus(Status.EXECUTED);
                schedulingRepository.save(scheduling);
                logger.info("payment executed successfully");
                String date = scheduling.getSchedulingDate().toString();
                String originEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getOriginAccount())
                        .orElseThrow(() -> new UserNotFound("User of origin payment email not found"));

                String destinationEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getDestinationAccount())
                        .orElseThrow(() -> new UserNotFound("User of destination payment email not found"));
                emailService.sendEmail(originEmail, subject, body + " was executed!!, on the date: " + date);
                emailService.sendEmail(destinationEmail, subject, body + " was executed!!, on the date: " + date);
            } else {
                scheduling.setStatus(Status.FAILED);
                schedulingRepository.save(scheduling);
                logger.warn("payment failed");
                String date = scheduling.getSchedulingDate().toString();
                String originEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getOriginAccount())
                        .orElseThrow(() -> new UserNotFound("User of origin payment email not found"));
                String destinationEmail = accountRepository
                        .findUserEmailByAccountNumber(scheduling.getDestinationAccount())
                        .orElseThrow(() -> new UserNotFound("User of destination payment email not found"));
                emailService.sendEmail(originEmail, subject, body + " was failed!!, on the date: " + date);
                emailService.sendEmail(destinationEmail, subject, body + " was failed!!, on the date: " + date);

            }
        } catch (Exception e) {
            logger.error("Error executing payment");
            scheduling.setStatus(Status.FAILED);
            schedulingRepository.save(scheduling);
            String date = scheduling.getSchedulingDate().toString();
            String originEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getOriginAccount())
                    .orElseThrow(() -> new UserNotFound("User of origin payment email not found"));
            String destinationEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getDestinationAccount())
                    .orElseThrow(() -> new UserNotFound("User of destination payment email not found"));
            emailService.sendEmail(originEmail, subject, body + " was failed!!, on the date: " + date);
            emailService.sendEmail(destinationEmail, subject, body + " was failed!!, on the date: " + date);
        } finally {
            scheduledJobs.remove(scheduling.getId());
        }
    }

    public void cancelScheduledJob(Long schedulingId) {
        ScheduledFuture<?> future = scheduledJobs.get(schedulingId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            scheduledJobs.remove(schedulingId);
            logger.info("Cancelled scheduled job");
        }
    }
}

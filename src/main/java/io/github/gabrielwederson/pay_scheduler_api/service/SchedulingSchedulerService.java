package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.exception.AccountNotFound;
import io.github.gabrielwederson.pay_scheduler_api.exception.UserNotFoundException;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.SchedulingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;
    private final Map<Long, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private static final String subject = "NEWS ABOUT SCHEDULED PAYMENT";
    private static final String body = "Your scheduled payment";

    public SchedulingSchedulerService(TaskScheduler taskScheduler,
                                      SchedulingRepository schedulingRepository,
                                      AccountRepository accountRepository,
                                      EmailService emailService,
                                      PlatformTransactionManager transactionManager) {
        this.taskScheduler = taskScheduler;
        this.schedulingRepository = schedulingRepository;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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
        logger.info("Scheduled verification for scheduling");
    }

    private void scheduleExactPayment(Scheduling scheduling) {
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executePaymentWithTransaction(scheduling),
                Date.from(scheduling.getSchedulingDate().atZone(ZoneId.systemDefault()).toInstant())
        );
        scheduledJobs.put(scheduling.getId(), future);
        logger.info("Scheduled exact payment for scheduling ");
    }

    private void executeVerification(Scheduling scheduling) {
        logger.info("Executing verification for scheduling ");

        try {
            if (scheduling.getStatus() == Status.PENDING) {
                scheduleExactPayment(scheduling);
                logger.info("Payment scheduled for execution at");
            } else {
                logger.warn("Scheduling {} is not pending, status");
            }
        } catch (Exception e) {
            logger.error("Error during verification for scheduling ");

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    releaseReservation(scheduling, scheduling.getOriginAccount());
                    scheduling.setStatus(Status.FAILED);
                    schedulingRepository.save(scheduling);
                }
            });
        }
    }


    private void executePaymentWithTransaction(Scheduling scheduling) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                executePayment(scheduling);
            }
        });
    }


    private void executePayment(Scheduling scheduling) {
        logger.info("Executing payment for scheduling");

        try {
            Account origin = accountRepository
                    .findAccountByNumberWithLock(scheduling.getOriginAccount().getNumberAccount())
                    .orElseThrow(() -> new AccountNotFound("Origin account not found"));

            Account destination = accountRepository
                    .findAccountByNumberWithLock(scheduling.getDestinationAccount().getNumberAccount())
                    .orElseThrow(() -> new AccountNotFound("Destination account not found"));

            if (origin.getReservedBalance().compareTo(scheduling.getValue()) < 0) {
                logger.warn("Reservation not found or already released for scheduling");
                scheduling.setStatus(Status.FAILED);
                schedulingRepository.save(scheduling);
                return;
            }

            origin.setBalance(origin.getBalance().subtract(scheduling.getValue()));
            origin.setReservedBalance(origin.getReservedBalance().subtract(scheduling.getValue()));
            accountRepository.save(origin);

            destination.setBalance(destination.getBalance().add(scheduling.getValue()));
            accountRepository.save(destination);

            scheduling.setStatus(Status.EXECUTED);
            schedulingRepository.save(scheduling);

            logger.info("Payment executed successfully for scheduling");

            sendSuccessEmail(scheduling);

        } catch (Exception e) {
            logger.error("Error executing payment for scheduling");
            scheduling.setStatus(Status.FAILED);
            schedulingRepository.save(scheduling);
            sendFailureEmail(scheduling);
            throw new RuntimeException("Payment failed", e);
        } finally {
            scheduledJobs.remove(scheduling.getId());
        }
    }

    private void releaseReservation(Scheduling scheduling, Account origin) {
        origin.setReservedBalance(origin.getReservedBalance().subtract(scheduling.getValue()));
        accountRepository.save(origin);
        logger.info("Reservation released for scheduling");
    }

    public void cancelScheduledJob(Long schedulingId) {
        ScheduledFuture<?> future = scheduledJobs.get(schedulingId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
            scheduledJobs.remove(schedulingId);
            logger.info("Cancelled scheduled job for scheduling");
        }
    }

    public void cancelScheduling(Long schedulingId) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Scheduling scheduling = schedulingRepository.findById(schedulingId)
                        .orElseThrow(() -> new RuntimeException("Scheduling not found"));

                if (scheduling.getStatus() != Status.PENDING) {
                    throw new RuntimeException("Only pending scheduling can be cancelled");
                }

                Account origin = scheduling.getOriginAccount();
                origin.setReservedBalance(origin.getReservedBalance().subtract(scheduling.getValue()));
                accountRepository.save(origin);

                cancelScheduledJob(schedulingId);

                scheduling.setStatus(Status.FAILED);
                schedulingRepository.save(scheduling);

                logger.info("Scheduling {} cancelled by user", schedulingId);
            }
        });
    }

    private void sendFailureEmail(Scheduling scheduling) {
        try {
            String date = scheduling.getSchedulingDate().toString();
            String originEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getOriginAccount().getNumberAccount())
                    .orElseThrow(() -> new UserNotFoundException("User of origin payment email not found"));
            String destinationEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getDestinationAccount().getNumberAccount())
                    .orElseThrow(() -> new UserNotFoundException("User of destination payment email not found"));

            emailService.sendEmail(originEmail, subject, body + " was failed on: " + date);
            emailService.sendEmail(destinationEmail, subject, body + " was failed on: " + date);
        } catch (Exception e) {
            logger.error("Error sending failure email for scheduling ");
        }
    }

    private void sendSuccessEmail(Scheduling scheduling) {
        try {
            String date = scheduling.getSchedulingDate().toString();
            String originEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getOriginAccount().getNumberAccount())
                    .orElseThrow(() -> new UserNotFoundException("User of origin payment email not found"));
            String destinationEmail = accountRepository
                    .findUserEmailByAccountNumber(scheduling.getDestinationAccount().getNumberAccount())
                    .orElseThrow(() -> new UserNotFoundException("User of destination payment email not found"));

            emailService.sendEmail(originEmail, subject, body + " was executed on: " + date);
            emailService.sendEmail(destinationEmail, subject, body + " was executed on: " + date);
        } catch (Exception e) {
            logger.error("Error sending success email for scheduling ");
        }
    }
}

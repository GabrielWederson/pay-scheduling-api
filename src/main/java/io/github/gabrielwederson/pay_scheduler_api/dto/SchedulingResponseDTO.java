package io.github.gabrielwederson.pay_scheduler_api.dto;

import io.github.gabrielwederson.pay_scheduler_api.model.Scheduling;
import io.github.gabrielwederson.pay_scheduler_api.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originAccount;
    private String destinationAccount;
    private BigDecimal value;
    private LocalDateTime createdAt;
    private LocalDateTime schedulingDate;
    private Status status;

    public SchedulingResponseDTO() {
    }


    public SchedulingResponseDTO(Scheduling scheduling) {
        this.originAccount = scheduling.getOriginAccount().getNumberAccount();
        this.destinationAccount = scheduling.getDestinationAccount().getNumberAccount();
        this.value = scheduling.getValue();
        this.createdAt = scheduling.getCreatedAt();
        this.schedulingDate = scheduling.getSchedulingDate();
        this.status = scheduling.getStatus();
    }

    public String getOriginAccount() {
        return originAccount;
    }

    public void setOriginAccount(String originAccount) {
        this.originAccount = originAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSchedulingDate() {
        return schedulingDate;
    }

    public void setSchedulingDate(LocalDateTime schedulingDate) {
        this.schedulingDate = schedulingDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingResponseDTO that = (SchedulingResponseDTO) o;
        return Objects.equals(originAccount, that.originAccount) &&
                Objects.equals(destinationAccount, that.destinationAccount) &&
                Objects.equals(value, that.value) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(schedulingDate, that.schedulingDate) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originAccount, destinationAccount, value, createdAt, schedulingDate, status);
    }
}

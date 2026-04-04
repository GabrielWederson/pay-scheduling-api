package io.github.gabrielwederson.pay_scheduler_api.dto;

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

    private LocalDateTime created_at;

    private LocalDateTime schedulingDate;

    private Status status;

    public SchedulingResponseDTO() {
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

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
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
        return Objects.equals(originAccount, that.originAccount) && Objects.equals(destinationAccount, that.destinationAccount) && Objects.equals(value, that.value) && Objects.equals(created_at, that.created_at) && Objects.equals(schedulingDate, that.schedulingDate) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(originAccount, destinationAccount, value, created_at, schedulingDate, status);
    }
}

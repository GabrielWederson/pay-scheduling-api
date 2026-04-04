package io.github.gabrielwederson.pay_scheduler_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class SchedulingRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @NotEmpty
    @NotBlank
    private String originAccount;

    @NotNull
    @NotEmpty
    @NotBlank
    private String destinationAccount;

    @Positive
    private BigDecimal value;

    @NotNull
    @NotEmpty
    @NotBlank
    private LocalDateTime schedulingDate;

    public SchedulingRequestDTO() {
    }

    public SchedulingRequestDTO(String originAccount, String destinationAccount, BigDecimal value, LocalDateTime schedulingDate) {
        this.originAccount = originAccount;
        this.destinationAccount = destinationAccount;
        this.value = value;
        this.schedulingDate = schedulingDate;
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

    public LocalDateTime getSchedulingDate() {
        return schedulingDate;
    }

    public void setSchedulingDate(LocalDateTime schedulingDate) {
        this.schedulingDate = schedulingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingRequestDTO that = (SchedulingRequestDTO) o;
        return Objects.equals(originAccount, that.originAccount) && Objects.equals(destinationAccount, that.destinationAccount) && Objects.equals(value, that.value) && Objects.equals(schedulingDate, that.schedulingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originAccount, destinationAccount, value, schedulingDate);
    }
}

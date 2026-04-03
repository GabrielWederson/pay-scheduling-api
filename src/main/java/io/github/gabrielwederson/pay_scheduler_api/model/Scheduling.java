package io.github.gabrielwederson.pay_scheduler_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "scheduling")
public class Scheduling implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_account")
    private String originAccount;

    @Column(name = "destination_account")
    private String destinationAccount;

    @Positive
    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "scheduling_date")
    private LocalDateTime schedulingDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Scheduling() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        Scheduling that = (Scheduling) o;
        return Objects.equals(id, that.id) && Objects.equals(originAccount, that.originAccount) && Objects.equals(destinationAccount, that.destinationAccount) && Objects.equals(value, that.value) && Objects.equals(created_at, that.created_at) && Objects.equals(schedulingDate, that.schedulingDate) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, originAccount, destinationAccount, value, created_at, schedulingDate, status);
    }
}

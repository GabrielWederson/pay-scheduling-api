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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_account_id", nullable = false)
    private Account originAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Positive
    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "scheduling_date", nullable = false)
    private LocalDateTime schedulingDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getOriginAccount() {
        return originAccount;
    }
    public void setOriginAccount(Account originAccount) {
        this.originAccount = originAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(Account destinationAccount) {
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Status.PENDING;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Scheduling that = (Scheduling) o;
        return Objects.equals(id, that.id) && Objects.equals(originAccount, that.originAccount) && Objects.equals(destinationAccount, that.destinationAccount) && Objects.equals(value, that.value) && Objects.equals(createdAt, that.createdAt) && Objects.equals(schedulingDate, that.schedulingDate) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, originAccount, destinationAccount, value, createdAt, schedulingDate, status);
    }
}

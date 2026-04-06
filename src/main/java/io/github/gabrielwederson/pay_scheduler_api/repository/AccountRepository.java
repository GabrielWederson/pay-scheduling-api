package io.github.gabrielwederson.pay_scheduler_api.repository;

import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.numberAccount =:numberAccount")
    Optional<Account> findAccountByNumber(@Param("numberAccount")String numberAccount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.numberAccount = :numberAccount")
    Optional<Account> findAccountByNumberWithLock(@Param("numberAccount") String numberAccount);

    @Query("SELECT u.email FROM Account a JOIN a.users u WHERE a.numberAccount = :numberAccount")
    Optional<String> findUserEmailByAccountNumber(@Param("numberAccount") String numberAccount);
}

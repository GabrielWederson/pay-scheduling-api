package io.github.gabrielwederson.pay_scheduler_api.repository;

import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE a.numberAccount =:numberAccount")
    Optional<Account> findAccountByNumber(@Param("numberAccount")String numberAccount);
}

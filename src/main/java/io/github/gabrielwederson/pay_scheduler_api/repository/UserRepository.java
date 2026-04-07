package io.github.gabrielwederson.pay_scheduler_api.repository;

import io.github.gabrielwederson.pay_scheduler_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u.email FROM User u JOIN u.accounts a WHERE a.numberAccount = :accountNumber")
    Optional<String> findUserEmailByAccountNumber(@Param("accountNumber") String accountNumber);
}

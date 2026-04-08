package io.github.gabrielwederson.pay_scheduler_api.service;


import io.github.gabrielwederson.pay_scheduler_api.dto.RegisterRequest;
import io.github.gabrielwederson.pay_scheduler_api.dto.SignInRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidDataException;
import io.github.gabrielwederson.pay_scheduler_api.exception.InvalidRefreshTokenException;
import io.github.gabrielwederson.pay_scheduler_api.exception.UserNotFoundException;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.User;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.UserRepository;
import io.github.gabrielwederson.pay_scheduler_api.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    public TokenDTO register(RegisterRequest request) {
        logger.info("Registering new user");

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(List.of("USER"));

        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setNumberAccount(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setReservedBalance(BigDecimal.ZERO);
        account.setUser(savedUser);
        accountRepository.save(account);


        var token = tokenProvider.createAccessToken(
                savedUser.getEmail(),
                savedUser.getRoles()
        );

        logger.info("User registered and authenticated");
        return token;
    }

    public TokenDTO signin(SignInRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found" ));

        var token = tokenProvider.createAccessToken(
                user.getEmail(),
                user.getRoles()
        );

        return token;
    }

    private String generateAccountNumber() {
        return "000" + System.currentTimeMillis();
    }

    public TokenDTO refreshToken(String email, String refreshToken) {
        validateParameters(email, refreshToken);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Email not found!"));

        validateUserStatus(user);

        try {
            TokenDTO token = tokenProvider.refreshToken(refreshToken);

            if (token == null) {
                throw new InvalidRefreshTokenException("Failed to refresh token");
            }

            return token;

        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
    }

    private void validateParameters(String email, String refreshToken) {
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("Email cannot be null or empty");
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidDataException("Refresh token cannot be null or empty");
        }
    }

    private void validateUserStatus(io.github.gabrielwederson.pay_scheduler_api.model.User user) {
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("User account is locked");
        }

        if (!user.isAccountNonExpired()) {
            throw new RuntimeException("User account is expired");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new RuntimeException("User credentials are expired");
        }
    }
}

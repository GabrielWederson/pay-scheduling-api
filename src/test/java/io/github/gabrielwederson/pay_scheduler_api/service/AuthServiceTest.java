package io.github.gabrielwederson.pay_scheduler_api.service;

import io.github.gabrielwederson.pay_scheduler_api.dto.RegisterRequest;
import io.github.gabrielwederson.pay_scheduler_api.dto.SignInRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.github.gabrielwederson.pay_scheduler_api.model.Account;
import io.github.gabrielwederson.pay_scheduler_api.model.User;
import io.github.gabrielwederson.pay_scheduler_api.repository.AccountRepository;
import io.github.gabrielwederson.pay_scheduler_api.repository.UserRepository;
import io.github.gabrielwederson.pay_scheduler_api.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private SignInRequestDTO signInRequest;
    private User user;
    private Account account;
    private TokenDTO expectedToken;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John Doe",
                "john@example.com",
                "password123"
        );

        signInRequest = new SignInRequestDTO(
                "john@example.com",
                "password123"
        );

        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRoles(List.of("USER"));

        account = new Account();
        account.setId(1L);
        account.setNumberAccount("000123456789");
        account.setBalance(BigDecimal.ZERO);
        account.setReservedBalance(BigDecimal.ZERO);
        account.setUser(user);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1 hora depois
        expectedToken = new TokenDTO(
                "john@example.com",
                true,
                now,
                expiration,
                "access-token-123",
                "refresh-token-123"
        );
    }

    @Test
    void register_WithValidData_ShouldRegisterUserAndCreateAccount() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(tokenProvider.createAccessToken(user.getEmail(), user.getRoles())).thenReturn(expectedToken);

        TokenDTO result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(expectedToken, result);
        assertEquals("john@example.com", result.getUsername());
        assertTrue(result.getAuthenticated());
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
        verify(tokenProvider).createAccessToken(user.getEmail(), user.getRoles());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void register_ShouldCreateAccountWithCorrectValues() {
        
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(tokenProvider.createAccessToken(anyString(), any())).thenReturn(expectedToken);

      
        authService.register(registerRequest);

        
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertNotNull(capturedAccount.getNumberAccount());
        assertEquals(BigDecimal.ZERO, capturedAccount.getBalance());
        assertEquals(BigDecimal.ZERO, capturedAccount.getReservedBalance());
        assertEquals(user, capturedAccount.getUser());
    }

    @Test
    void signin_WithValidCredentials_ShouldReturnToken() {
        
        when(userRepository.findByEmail(signInRequest.email())).thenReturn(Optional.of(user));
        when(tokenProvider.createAccessToken(user.getEmail(), user.getRoles())).thenReturn(expectedToken);

      
        TokenDTO result = authService.signin(signInRequest);

        
        assertNotNull(result);
        assertEquals(expectedToken, result);
        assertEquals("john@example.com", result.getUsername());
        assertTrue(result.getAuthenticated());
        assertNotNull(result.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(signInRequest.email());
        verify(tokenProvider).createAccessToken(user.getEmail(), user.getRoles());
    }

    @Test
    void signin_WithInvalidEmail_ShouldThrowUsernameNotFoundException() {
        
        when(userRepository.findByEmail(signInRequest.email())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authService.signin(signInRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(signInRequest.email());
        verify(tokenProvider, never()).createAccessToken(anyString(), any());
    }

    @Test
    void generateAccountNumber_ShouldReturnUniqueNumber() {
        
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(tokenProvider.createAccessToken(anyString(), any())).thenReturn(expectedToken);

      
        authService.register(registerRequest);

        
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        String accountNumber = accountCaptor.getValue().getNumberAccount();
        assertNotNull(accountNumber);
        assertTrue(accountNumber.startsWith("000"));
    }
}

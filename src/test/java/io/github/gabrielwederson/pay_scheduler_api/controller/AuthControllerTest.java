package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.dto.RegisterRequest;
import io.github.gabrielwederson.pay_scheduler_api.dto.SignInRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.github.gabrielwederson.pay_scheduler_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private SignInRequestDTO signInRequest;
    private TokenDTO tokenDTO;

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
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1 hora depois
        tokenDTO = new TokenDTO(
                "john@example.com",
                true,
                now,
                expiration,
                "access-token-123",
                "refresh-token-123"
        );
    }

    @Test
    void register_ShouldReturnCreatedStatusWithToken() {
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenDTO);

        
        ResponseEntity<TokenDTO> response = authController.register(registerRequest);

        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tokenDTO, response.getBody());
        assertEquals("john@example.com", response.getBody().getUsername());
        assertTrue(response.getBody().getAuthenticated());
        assertEquals("access-token-123", response.getBody().getAccessToken());

        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void register_ShouldCallAuthServiceWithCorrectRequest() {
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenDTO);

        
        authController.register(registerRequest);

        
        verify(authService, times(1)).register(registerRequest);
        verify(authService, never()).signin(any(SignInRequestDTO.class));
    }

    @Test
    void signin_ShouldReturnOkStatusWithToken() {
        
        when(authService.signin(any(SignInRequestDTO.class))).thenReturn(tokenDTO);

        
        ResponseEntity<TokenDTO> response = authController.signin(signInRequest);

        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tokenDTO, response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());

        verify(authService, times(1)).signin(signInRequest);
    }

    @Test
    void signin_ShouldCallAuthServiceWithCorrectRequest() {
        
        when(authService.signin(any(SignInRequestDTO.class))).thenReturn(tokenDTO);

        
        authController.signin(signInRequest);

        
        verify(authService, times(1)).signin(signInRequest);
        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void register_WhenAuthServiceThrowsException_ShouldPropagateException() {
        
        RuntimeException expectedException = new RuntimeException("Email already exists");
        when(authService.register(any(RegisterRequest.class))).thenThrow(expectedException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.register(registerRequest);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void signin_WhenAuthServiceThrowsException_ShouldPropagateException() {
        
        RuntimeException expectedException = new RuntimeException("Invalid credentials");
        when(authService.signin(any(SignInRequestDTO.class))).thenThrow(expectedException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.signin(signInRequest);
        });

        assertEquals(expectedException.getMessage(), exception.getMessage());
        verify(authService, times(1)).signin(signInRequest);
    }

    @Test
    void register_ShouldReturnTokenWithAllFields() {
        
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenDTO);

        
        ResponseEntity<TokenDTO> response = authController.register(registerRequest);
        TokenDTO returnedToken = response.getBody();

        
        assertNotNull(returnedToken);
        assertEquals("john@example.com", returnedToken.getUsername());
        assertTrue(returnedToken.getAuthenticated());
        assertNotNull(returnedToken.getCreated());
        assertNotNull(returnedToken.getExpiration());
        assertEquals("access-token-123", returnedToken.getAccessToken());
        assertEquals("refresh-token-123", returnedToken.getRefreshToken());
    }
}

package io.github.gabrielwederson.pay_scheduler_api.controller;

import io.github.gabrielwederson.pay_scheduler_api.controller.docs.AuthControllerDocs;
import io.github.gabrielwederson.pay_scheduler_api.dto.RegisterRequest;
import io.github.gabrielwederson.pay_scheduler_api.dto.SignInRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.RegisterResponseDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.github.gabrielwederson.pay_scheduler_api.model.User;
import io.github.gabrielwederson.pay_scheduler_api.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController implements AuthControllerDocs {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Override
    public ResponseEntity<TokenDTO> register(@Valid @RequestBody RegisterRequest request) {
        TokenDTO token = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/signin")
    @Override
    public ResponseEntity<TokenDTO> signin(@Valid @RequestBody SignInRequestDTO request) {
        System.out.println("(controller)Email recebido: " + request.email());
        System.out.println("(controller)Password recebido: " + request.password());

        TokenDTO token = authService.signin(request);
        return ResponseEntity.ok(token);
    }
}

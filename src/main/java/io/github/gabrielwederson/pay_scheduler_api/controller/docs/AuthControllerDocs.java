package io.github.gabrielwederson.pay_scheduler_api.controller.docs;

import io.github.gabrielwederson.pay_scheduler_api.dto.RegisterRequest;
import io.github.gabrielwederson.pay_scheduler_api.dto.SignInRequestDTO;
import io.github.gabrielwederson.pay_scheduler_api.dto.security.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

public interface AuthControllerDocs {

    @Operation(
            summary = "Authenticates a user and returns a token",
            description = "Validates user credentials and generates an access token for authentication.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<?> signin(SignInRequestDTO request);

    @Operation(
            summary = "Registers a new user and returns a token",
            description = "Creates a new user account and generates an access token for immediate authentication.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Created", responseCode = "201", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Conflict", responseCode = "409", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<?> register( RegisterRequest request);

    @Operation(
            summary = "Use for refresh token",
            description = "When you access token end, use this endpoint for access other methods using you refresh token",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Created", responseCode = "201", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Conflict", responseCode = "409", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    public ResponseEntity<TokenDTO> refreshToken(String email, String refreshToken);
}

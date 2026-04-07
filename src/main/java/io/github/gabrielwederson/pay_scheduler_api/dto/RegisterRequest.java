package io.github.gabrielwederson.pay_scheduler_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank @Email String email, @NotBlank String name, @NotBlank String password) {
}

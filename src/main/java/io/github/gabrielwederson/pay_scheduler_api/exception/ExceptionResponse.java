package io.github.gabrielwederson.pay_scheduler_api.exception;

import java.util.Date;

public record ExceptionResponse(Date timestamp, String message, String description) {
}

package com.smartbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.smartbank.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

   @ExceptionHandler(AccountNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public ErrorResponseDTO handleAccountNotFound(AccountNotFoundException ex) {

    return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(404)
            .error("ACCOUNT_NOT_FOUND")
            .message(ex.getMessage())
            .build();
}

  @ExceptionHandler(InsufficientBalanceException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponseDTO handleInsufficientBalance(InsufficientBalanceException ex) {

    return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("INSUFFICIENT_BALANCE")
            .message(ex.getMessage())
            .build();
}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleValidationExceptions(MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error -> {
        errors.put(error.getField(), error.getDefaultMessage());
    });

    return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("VALIDATION_ERROR")
            .message(errors)
            .build();
}

    @ExceptionHandler(Exception.class)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public ErrorResponseDTO handleGeneralException(Exception ex) {

    return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(500)
            .error("INTERNAL_ERROR")
            .message(ex.getMessage())
            .build();
}

@ExceptionHandler(DuplicateEmailException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ErrorResponseDTO handleDuplicateEmail(DuplicateEmailException ex) {

    return ErrorResponseDTO.builder()
            .timestamp(LocalDateTime.now())
            .status(400)
            .error("DUPLICATE_EMAIL")
            .message(ex.getMessage())
            .build();
}

}
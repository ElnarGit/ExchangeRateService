package com.example.ExchangeRateService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ExchangeRateNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleExchangeRateNotFoundException(ExchangeRateNotFoundException e) {
    return e.getMessage();
  }

  @ExceptionHandler(ResponseStatusException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleResponseStatusException(ResponseStatusException e) {
    return e.getMessage();
  }
}

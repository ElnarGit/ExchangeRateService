package com.example.ExchangeRateService.exception;

public class ExchangeRateNotFoundException extends RuntimeException {
  public ExchangeRateNotFoundException(String message) {
    super(message);
  }
}

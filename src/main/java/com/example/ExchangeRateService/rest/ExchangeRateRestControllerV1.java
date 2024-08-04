package com.example.ExchangeRateService.rest;

import com.example.ExchangeRateService.exception.ExchangeRateNotFoundException;
import com.example.ExchangeRateService.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeRateRestControllerV1 {

  private final ExchangeRateService exchangeRateService;

  @GetMapping("/{currency}")
  @ResponseStatus(HttpStatus.OK)
  public double getRate(@PathVariable String currency) {
    try {
      return exchangeRateService.getExchangeRate(currency.toUpperCase());
    } catch (ResponseStatusException e) {
      throw new ExchangeRateNotFoundException("Currency not found");
    }
  }

  @GetMapping("/convert")
  @ResponseStatus(HttpStatus.OK)
  public double convert(
      @RequestParam double amount, @RequestParam String from, @RequestParam String to) {
    try {
      return exchangeRateService.convertCurrency(amount, from.toUpperCase(), to.toUpperCase());
    } catch (ResponseStatusException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversion failed");
    }
  }
}

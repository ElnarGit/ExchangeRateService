package com.example.ExchangeRateService.rest;

import com.example.ExchangeRateService.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
public class ExchangeRateRestControllerV1 {

  private final ExchangeRateService exchangeRateService;

  @GetMapping("/{currency}")
  public double getRate(@PathVariable String currency) {
    return exchangeRateService.getExchangeRate(currency.toUpperCase());
  }

  @GetMapping("/convert")
  public double convert(
      @RequestParam double amount, @RequestParam String from, @RequestParam String to) {
    return exchangeRateService.convertCurrency(amount, from.toUpperCase(), to.toUpperCase());
  }
}

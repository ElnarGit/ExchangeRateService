package com.example.ExchangeRateService.serivce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.example.ExchangeRateService.entity.ExchangeRate;
import com.example.ExchangeRateService.exception.ExchangeRateNotFoundException;
import com.example.ExchangeRateService.repository.ExchangeRateRepository;
import com.example.ExchangeRateService.service.ExchangeRateService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTests {

  @Mock private ExchangeRateRepository exchangeRateRepository;

  private ExchangeRateService exchangeRateService;

  private ExchangeRate exchangeRate;

  @BeforeEach
  void setUp() {
    exchangeRate =
        ExchangeRate.builder().currency("USD").rate(470).date(LocalDateTime.now()).build();

    exchangeRateService =
        new ExchangeRateService(
            exchangeRateRepository, "https://nationalbank.kz/rss/rates_all.xml");
  }

  @Test
  @DisplayName("Get exchange rate successfully when currency is found")
  void getExchangeRateFound() {
    when(exchangeRateRepository.findByCurrency("USD")).thenReturn(Optional.of(exchangeRate));

    double rate = exchangeRateService.getExchangeRate("USD");

    assertEquals(470, rate);
    verify(exchangeRateRepository, times(1)).findByCurrency("USD");
  }

  @Test
  @DisplayName("Throw exception when exchange rate for currency is not found")
  void getExchangeRateNotFound() {
    when(exchangeRateRepository.findByCurrency("USD")).thenReturn(Optional.empty());

    Exception exception =
        assertThrows(
            ExchangeRateNotFoundException.class, () -> exchangeRateService.getExchangeRate("USD"));

    assertEquals("Exchange rate not found for currency: USD", exception.getMessage());
    verify(exchangeRateRepository, times(1)).findByCurrency("USD");
  }

  @Test
  @DisplayName("Convert currency amount correctly")
  void convertCurrency() {
    when(exchangeRateRepository.findByCurrency("USD")).thenReturn(Optional.of(exchangeRate));
    when(exchangeRateRepository.findByCurrency("EUR"))
        .thenReturn(
            Optional.of(
                ExchangeRate.builder()
                    .currency("EUR")
                    .rate(500)
                    .date(LocalDateTime.now())
                    .build()));

    double convertedAmount = exchangeRateService.convertCurrency(100, "USD", "EUR");

    assertEquals(94.0, convertedAmount);
    verify(exchangeRateRepository, times(1)).findByCurrency("USD");
    verify(exchangeRateRepository, times(1)).findByCurrency("EUR");
  }
}

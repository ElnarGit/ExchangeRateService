package com.example.ExchangeRateService.service;

import com.example.ExchangeRateService.entity.ExchangeRate;
import com.example.ExchangeRateService.exception.ExchangeRateNotFoundException;
import com.example.ExchangeRateService.repository.ExchangeRateRepository;
import com.example.ExchangeRateService.util.Item;
import com.example.ExchangeRateService.util.Rss;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ExchangeRateService {

  private static final Unmarshaller UNMARSHALLER;
  private final ExchangeRateRepository exchangeRateRepository;
  private final URL nationBankRatesUrl;

  static {
    try {
      JAXBContext context = JAXBContext.newInstance(Rss.class);
      UNMARSHALLER = context.createUnmarshaller();
    } catch (JAXBException e) {
      log.error("Could not create JAXB Unmarshaller", e);
      throw new RuntimeException("Could not create JAXB context", e);
    }
  }

  public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
      @Value("${national-bank.rates-url}") String ratesUrl) {
    this.exchangeRateRepository = exchangeRateRepository;
    try {
      this.nationBankRatesUrl = new URL(ratesUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Could not create URL", e);
    }
  }

  @Transactional
  @Scheduled(cron = "${loader.crone}")
  public void fetchExchangeRates() {
    log.info("Starting fetchExchangeRates task");

    Optional<ExchangeRate> latestRate = exchangeRateRepository.findTopByOrderByDateDesc();
    
    if (latestRate.isPresent()
        && Duration.between(latestRate.get().getDate(), LocalDateTime.now()).toDays() < 1) {
      log.info("Exchange rates are up to date. Skipping fetch.");
      return;
    }

    Rss rss;
    try {
      rss = (Rss) UNMARSHALLER.unmarshal(nationBankRatesUrl);
    } catch (JAXBException e) {
      throw new RuntimeException("Could not unmarshal Rss", e);
    }

    LocalDateTime now = LocalDateTime.now();

    // Удаление старых записей
    exchangeRateRepository.deleteByDateBefore(now);
    log.info("Old exchange rates deleted");

    for (Item item : rss.getChannel().getItem()) {
      double rate = Double.parseDouble(item.getDescription()) / Integer.parseInt(item.getQuant());
      ExchangeRate exchangeRate =
          ExchangeRate.builder().currency(item.getTitle()).rate(rate).date(now).build();

      log.info("Saving exchange rate for currency: {} with rate: {}", item.getTitle(), rate);
      exchangeRateRepository.save(exchangeRate);
    }
    log.info("Exchange rates fetch completed");
  }

  public double getExchangeRate(String currency) {
    log.info("Fetching exchange rate for currency: {}", currency);

    Optional<ExchangeRate> exchangeRate = exchangeRateRepository.findByCurrency(currency);
    if (exchangeRate.isPresent()) {
      log.info("Exchange rate found for currency: {} with rate: {}",
          currency, exchangeRate.get().getRate());

      return exchangeRate.get().getRate();
    } else {
      log.error("Exchange rate not found for currency: {}", currency);
      throw new ExchangeRateNotFoundException("Exchange rate not found for currency: " + currency);
    }
  }

  public double convertCurrency(double amount, String from, String to) {
    log.info("Converting amount: {} from currency: {} to currency: {}", amount, from, to);

    double fromRate = getExchangeRate(from);
    double toRate = getExchangeRate(to);
    double convertedAmount = amount * fromRate / toRate;

    log.info("Converted amount: {} from currency: {} to currency: {} is: {}",
        amount, from, to, convertedAmount);

    return convertedAmount;
  }
}

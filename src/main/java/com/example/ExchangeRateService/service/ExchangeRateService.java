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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExchangeRateService {

  public static final String EXCHANGE_RATE_URL = "https://nationalbank.kz/rss/rates_all.xml";

  private final ExchangeRateRepository exchangeRateRepository;

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void fetchExchangeRates() throws JAXBException, MalformedURLException {
    log.info("Starting fetchExchangeRates task");

    Optional<ExchangeRate> latestRate = exchangeRateRepository.findTopByOrderByDateDesc();
    if (latestRate.isPresent()
        && Duration.between(latestRate.get().getDate(), LocalDateTime.now()).toDays() < 1) {
      log.info("Exchange rates are up to date. Skipping fetch.");
      return;
    }

    JAXBContext context = JAXBContext.newInstance(Rss.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    URL url = new URL(EXCHANGE_RATE_URL);
    Rss rss = (Rss) unmarshaller.unmarshal(url);

    LocalDateTime now = LocalDateTime.now();
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
      log.warn("Exchange rate not found for currency: {}", currency);
      throw new ExchangeRateNotFoundException("Exchange rate not found for currency: " + currency);
    }
  }

  public double convertCurrency(double amount, String from, String to) {
    log.info("Converting amount: {} from currency: {} to currency: {}", amount, from, to);

    double fromRate = getExchangeRate(from);
    double toRate = getExchangeRate(to);
    double convertedAmount = (fromRate != 0 && toRate != 0) ? (amount / fromRate) * toRate : 0.0;

    log.info(
        "Converted amount: {} from currency: {} to currency: {} is: {}",
        amount,
        from,
        to,
        convertedAmount);
    
    return convertedAmount;
  }
}

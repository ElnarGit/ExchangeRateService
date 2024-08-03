package com.example.ExchangeRateService.repository;

import com.example.ExchangeRateService.entity.ExchangeRate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
  Optional<ExchangeRate> findByCurrency(String currency);

  Optional<ExchangeRate> findTopByOrderByDateDesc();
}

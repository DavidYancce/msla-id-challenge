package msla.challenge.mslachallenge.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import msla.challenge.mslachallenge.client.ExchangeRateClient;
import msla.challenge.mslachallenge.dto.ExternalApiResponse;
import msla.challenge.mslachallenge.entity.ExchangeHistory;
import msla.challenge.mslachallenge.entity.User;
import msla.challenge.mslachallenge.repository.ExchangeHistoryRepository;

@Service
@Transactional
public class ExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeHistoryRepository exchangeHistoryRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final UserService userService;

    @Value("${external.api.key}")
    private String apiKey;

    @Autowired
    public ExchangeService(ExchangeHistoryRepository exchangeHistoryRepository,
            ExchangeRateClient exchangeRateClient,
            UserService userService) {
        this.exchangeHistoryRepository = exchangeHistoryRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.userService = userService;
    }

    public ExchangeHistory convertCurrency(String fromCurrency, String toCurrency,
            BigDecimal amount, Long userId) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        try {
            ExternalApiResponse apiResponse;
            try {
                apiResponse = exchangeRateClient.convertCurrency(toCurrency, fromCurrency, amount, apiKey);

                if (!apiResponse.isSuccess()) {
                    apiResponse = getMockedExchangeRate(fromCurrency, toCurrency, amount);
                }
            } catch (Exception e) {
                apiResponse = getMockedExchangeRate(fromCurrency, toCurrency, amount);
            }

            if (!apiResponse.isSuccess()) {
                throw new RuntimeException("Error en la API externa: conversión falló");
            }

            BigDecimal exchangeRate = apiResponse.getInfo().getRate();
            BigDecimal convertedAmount = apiResponse.getResult();

            ExchangeHistory exchangeHistory = new ExchangeHistory(fromCurrency, toCurrency, amount, convertedAmount, exchangeRate, user);

            ExchangeHistory saved = exchangeHistoryRepository.save(exchangeHistory);
            logger.info("Conversión guardada exitosamente con ID: {}", saved.getId());

            return saved;

        } catch (Exception e) {
            logger.error("Error al realizar conversión: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar la conversión: " + e.getMessage());
        }
    }

    public List<ExchangeHistory> getExchangeHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        if (!userService.findById(userId).isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            return exchangeHistoryRepository.findByUserIdAndDateRangeOrderByConversionDateDesc(
                    userId, startDateTime, endDateTime);
        } else {
            return exchangeHistoryRepository.findByUserIdOrderByConversionDateDesc(userId);
        }
    }

    public List<CurrencySummaryDTO> getCurrencySummary(Long userId, LocalDate startDate, LocalDate endDate) {
        if (!userService.findById(userId).isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        List<ExchangeHistoryRepository.CurrencySummaryProjection> projections;

        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            projections = exchangeHistoryRepository.findCurrencySummaryByUserIdAndDateRange(
                    userId, startDateTime, endDateTime);
        } else {
            projections = exchangeHistoryRepository.findCurrencySummaryByUserId(userId);
        }

        return projections.stream()
                .map(projection -> new CurrencySummaryDTO(
                        projection.getCurrency(),
                        projection.getTotalConverted(),
                        projection.getConversionCount().intValue()))
                .collect(Collectors.toList());
    }

    // Mock method used as fallback when API is unavailable
    private ExternalApiResponse getMockedExchangeRate(String fromCurrency, String toCurrency, BigDecimal amount) {
        logger.info("Usando datos mock para conversión {} -> {}", fromCurrency, toCurrency);

        ExternalApiResponse response = new ExternalApiResponse();
        response.setSuccess(true);
        response.setDate(LocalDate.now().toString());

        // Mock exchange rates
        BigDecimal rate = getMockExchangeRate(fromCurrency, toCurrency);
        BigDecimal result = amount.multiply(rate);

        ExternalApiResponse.Info info = new ExternalApiResponse.Info();
        info.setRate(rate);
        info.setTimestamp(System.currentTimeMillis() / 1000L);
        response.setInfo(info);

        ExternalApiResponse.Query query = new ExternalApiResponse.Query();
        query.setAmount(amount);
        query.setFrom(fromCurrency);
        query.setTo(toCurrency);
        response.setQuery(query);

        response.setResult(result);

        return response;
    }

    private BigDecimal getMockExchangeRate(String fromCurrency, String toCurrency) {
        if ("USD".equals(fromCurrency) && "PEN".equals(toCurrency)) {
            return new BigDecimal("3.739165");
        } else if ("PEN".equals(fromCurrency) && "USD".equals(toCurrency)) {
            return new BigDecimal("0.267427");
        } else if ("USD".equals(fromCurrency) && "EUR".equals(toCurrency)) {
            return new BigDecimal("0.85");
        } else if ("EUR".equals(fromCurrency) && "USD".equals(toCurrency)) {
            return new BigDecimal("1.18");
        } else {
            return new BigDecimal("1.0");
        }
    }

    // DTO for currency summary
    public static class CurrencySummaryDTO {
        private String currency;
        private BigDecimal totalConverted;
        private Integer conversionCount;

        public CurrencySummaryDTO(String currency, BigDecimal totalConverted, Integer conversionCount) {
            this.currency = currency;
            this.totalConverted = totalConverted;
            this.conversionCount = conversionCount;
        }

        // Getters
        public String getCurrency() {
            return currency;
        }

        public BigDecimal getTotalConverted() {
            return totalConverted;
        }

        public Integer getConversionCount() {
            return conversionCount;
        }
    }
}
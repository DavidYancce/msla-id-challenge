package msla.challenge.mslachallenge.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import msla.challenge.mslachallenge.entity.ExchangeHistory;
import msla.challenge.mslachallenge.entity.User;
import msla.challenge.mslachallenge.service.ExchangeService;
import msla.challenge.mslachallenge.service.UserService;

@RestController
@RequestMapping("/api/v1/exchange")
@CrossOrigin(origins = "*")
public class ExchangeController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeController.class);

    private final ExchangeService exchangeService;
    private final UserService userService;

    @Autowired
    public ExchangeController(ExchangeService exchangeService, UserService userService) {
        this.exchangeService = exchangeService;
        this.userService = userService;
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@RequestBody ConversionRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            ExchangeHistory exchangeHistory = exchangeService.convertCurrency(
                    request.getFrom(),
                    request.getTo(),
                    request.getAmount(),
                    user.getId());

            ConversionResponse response = new ConversionResponse(
                    exchangeHistory.getId(),
                    exchangeHistory.getConversionDate(),
                    exchangeHistory.getFromCurrency(),
                    exchangeHistory.getToCurrency(),
                    exchangeHistory.getOriginalAmount(),
                    exchangeHistory.getConvertedAmount(),
                    exchangeHistory.getExchangeRate());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error de validación", e.getMessage(), LocalDateTime.now()));
        } catch (Exception e) {
            logger.error("Error interno: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Error interno del servidor", e.getMessage(), LocalDateTime.now())
            );
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getExchangeHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            logger.info("Obteniendo historial para usuario {} desde {} hasta {}", user.getUsername(), startDate,
                    endDate);

            List<ExchangeHistory> history = exchangeService.getExchangeHistory(user.getId(), startDate, endDate);

            List<ExchangeHistoryResponse> response = history.stream()
                    .map(eh -> new ExchangeHistoryResponse(
                            eh.getId(),
                            eh.getConversionDate(),
                            eh.getFromCurrency(),
                            eh.getToCurrency(),
                            eh.getOriginalAmount(),
                            eh.getConvertedAmount(),
                            eh.getExchangeRate()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage(), LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Error interno del servidor", e.getMessage(), LocalDateTime.now()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getExchangeSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

            List<ExchangeService.CurrencySummaryDTO> summary = exchangeService.getCurrencySummary(user.getId(), startDate, endDate);

            List<CurrencySummaryResponse> response = summary.stream()
                    .map(s -> new CurrencySummaryResponse(s.getCurrency(), s.getTotalConverted(), s.getConversionCount()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("Error de validación", e.getMessage(), LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Error interno del servidor", e.getMessage(), LocalDateTime.now()));
        }
    }

    // DTOs for request/response
    public static class ConversionRequest {
        private String from;
        private String to;
        private BigDecimal amount;

        // Constructors, getters and setters
        public ConversionRequest() {
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public static class ConversionResponse {
        private Long id;
        private LocalDateTime date;
        private String from;
        private String to;
        private BigDecimal originalAmount;
        private BigDecimal convertedAmount;
        private BigDecimal exchangeRate;

        public ConversionResponse(Long id, LocalDateTime date, String from, String to,
                BigDecimal originalAmount, BigDecimal convertedAmount,
                BigDecimal exchangeRate) {
            this.id = id;
            this.date = date;
            this.from = from;
            this.to = to;
            this.originalAmount = originalAmount;
            this.convertedAmount = convertedAmount;
            this.exchangeRate = exchangeRate;
        }

        // Getters
        public Long getId() {
            return id;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public BigDecimal getOriginalAmount() {
            return originalAmount;
        }

        public BigDecimal getConvertedAmount() {
            return convertedAmount;
        }

        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }
    }

    public static class ExchangeHistoryResponse {
        private Long id;
        private LocalDateTime conversionDate;
        private String fromCurrency;
        private String toCurrency;
        private BigDecimal originalAmount;
        private BigDecimal convertedAmount;
        private BigDecimal exchangeRate;

        public ExchangeHistoryResponse(Long id, LocalDateTime conversionDate, String fromCurrency,
                String toCurrency, BigDecimal originalAmount,
                BigDecimal convertedAmount, BigDecimal exchangeRate) {
            this.id = id;
            this.conversionDate = conversionDate;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.originalAmount = originalAmount;
            this.convertedAmount = convertedAmount;
            this.exchangeRate = exchangeRate;
        }

        // Getters
        public Long getId() {
            return id;
        }

        public LocalDateTime getConversionDate() {
            return conversionDate;
        }

        public String getFromCurrency() {
            return fromCurrency;
        }

        public String getToCurrency() {
            return toCurrency;
        }

        public BigDecimal getOriginalAmount() {
            return originalAmount;
        }

        public BigDecimal getConvertedAmount() {
            return convertedAmount;
        }

        public BigDecimal getExchangeRate() {
            return exchangeRate;
        }
    }

    public static class CurrencySummaryResponse {
        private String currency;
        private BigDecimal totalConverted;
        private Integer conversionCount;

        public CurrencySummaryResponse(String currency, BigDecimal totalConverted, Integer conversionCount) {
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

    public static class ErrorResponse {
        private String message;
        private String details;
        private LocalDateTime timestamp;

        public ErrorResponse(String message, String details, LocalDateTime timestamp) {
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }

        // Getters
        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
package msla.challenge.mslachallenge.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.antlr.v4.runtime.misc.NotNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "exchange_history", schema = "exchange")
public class ExchangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversion_date", nullable = false)
    @NotNull
    private LocalDateTime conversionDate;

    @Column(name = "from_currency", nullable = false, length = 3)
    @NotBlank
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false, length = 3)
    @NotBlank
    private String toCurrency;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal originalAmount;

    @Column(name = "converted_amount", nullable = false, precision = 19, scale = 4)
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal convertedAmount;

    @Column(name = "exchange_rate", nullable = false, precision = 10, scale = 6)
    @NotNull
    @DecimalMin("0.000001")
    private BigDecimal exchangeRate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    // Constructors
    public ExchangeHistory() {
    }

    public ExchangeHistory(String fromCurrency, String toCurrency, BigDecimal originalAmount,
            BigDecimal convertedAmount, BigDecimal exchangeRate, User user) {
        this.conversionDate = LocalDateTime.now();
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.originalAmount = originalAmount;
        this.convertedAmount = convertedAmount;
        this.exchangeRate = exchangeRate;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        if (this.conversionDate == null) {
            this.conversionDate = LocalDateTime.now();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(LocalDateTime conversionDate) {
        this.conversionDate = conversionDate;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ExchangeHistory{" +
                "id=" + id +
                ", conversionDate=" + conversionDate +
                ", fromCurrency='" + fromCurrency + '\'' +
                ", toCurrency='" + toCurrency + '\'' +
                ", originalAmount=" + originalAmount +
                ", convertedAmount=" + convertedAmount +
                ", exchangeRate=" + exchangeRate +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }
}
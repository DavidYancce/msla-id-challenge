package msla.challenge.mslachallenge.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import msla.challenge.mslachallenge.entity.ExchangeHistory;

@Repository
public interface ExchangeHistoryRepository extends JpaRepository<ExchangeHistory, Long> {

    List<ExchangeHistory> findByUserIdOrderByConversionDateDesc(Long userId);

    @Query("SELECT eh FROM ExchangeHistory eh WHERE eh.user.id = :userId " +
            "AND eh.conversionDate >= :startDate AND eh.conversionDate <= :endDate " +
            "ORDER BY eh.conversionDate DESC")
    List<ExchangeHistory> findByUserIdAndDateRangeOrderByConversionDateDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT eh.toCurrency as currency, SUM(eh.convertedAmount) as totalConverted, COUNT(eh) as conversionCount "
            +
            "FROM ExchangeHistory eh WHERE eh.user.id = :userId " +
            "GROUP BY eh.toCurrency ORDER BY eh.toCurrency")
    List<CurrencySummaryProjection> findCurrencySummaryByUserId(@Param("userId") Long userId);

    @Query("SELECT eh.toCurrency as currency, SUM(eh.convertedAmount) as totalConverted, COUNT(eh) as conversionCount "
            +
            "FROM ExchangeHistory eh WHERE eh.user.id = :userId " +
            "AND eh.conversionDate >= :startDate AND eh.conversionDate <= :endDate " +
            "GROUP BY eh.toCurrency ORDER BY eh.toCurrency")
    List<CurrencySummaryProjection> findCurrencySummaryByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    interface CurrencySummaryProjection {
        String getCurrency();

        BigDecimal getTotalConverted();

        Long getConversionCount();
    }
}
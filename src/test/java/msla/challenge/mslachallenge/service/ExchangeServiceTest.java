package msla.challenge.mslachallenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import msla.challenge.mslachallenge.client.ExchangeRateClient;
import msla.challenge.mslachallenge.entity.ExchangeHistory;
import msla.challenge.mslachallenge.entity.User;
import msla.challenge.mslachallenge.repository.ExchangeHistoryRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ExchangeServiceTest {

        @Mock
        private ExchangeHistoryRepository exchangeHistoryRepository;

        @Mock
        private ExchangeRateClient exchangeRateClient;

        @Mock
        private UserService userService;

        private ExchangeService exchangeService;

        @BeforeEach
        void setUp() {
                exchangeService = new ExchangeService(exchangeHistoryRepository, exchangeRateClient, userService);
        }

        @Test
        void testConvertCurrency_Success() {
                // Given
                String fromCurrency = "USD";
                String toCurrency = "PEN";
                BigDecimal amount = new BigDecimal("100.00");
                Long userId = 1L;

                User user = new User("testuser", "password", "test@test.com", "Test", "User");
                user.setId(userId);

                ExchangeHistory savedHistory = new ExchangeHistory(fromCurrency, toCurrency, amount,
                                new BigDecimal("373.92"), new BigDecimal("3.739165"), user);
                savedHistory.setId(1L);

                when(userService.findById(userId)).thenReturn(Optional.of(user));
                when(exchangeHistoryRepository.save(any(ExchangeHistory.class))).thenReturn(savedHistory);

                // When
                ExchangeHistory result = exchangeService.convertCurrency(fromCurrency, toCurrency, amount, userId);

                // Then
                assertNotNull(result);
                assertEquals(fromCurrency, result.getFromCurrency());
                assertEquals(toCurrency, result.getToCurrency());
                assertEquals(amount, result.getOriginalAmount());
                assertEquals(user, result.getUser());
                verify(userService).findById(userId);
                verify(exchangeHistoryRepository).save(any(ExchangeHistory.class));
        }

        @Test
        void testConvertCurrency_UserNotFound() {
                // Given
                String fromCurrency = "USD";
                String toCurrency = "PEN";
                BigDecimal amount = new BigDecimal("100.00");
                Long userId = 999L;

                when(userService.findById(userId)).thenReturn(Optional.empty());

                // When & Then
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> exchangeService.convertCurrency(fromCurrency, toCurrency, amount, userId));

                assertTrue(exception.getMessage().contains("Usuario no encontrado"));
                verify(userService).findById(userId);
                verify(exchangeHistoryRepository, never()).save(any(ExchangeHistory.class));
        }

        @Test
        void testGetExchangeHistory_WithDateRange() {
                // Given
                Long userId = 1L;
                LocalDate startDate = LocalDate.of(2025, 1, 1);
                LocalDate endDate = LocalDate.of(2025, 1, 31);

                User user = new User("testuser", "password", "test@test.com", "Test", "User");
                user.setId(userId);

                ExchangeHistory history1 = new ExchangeHistory("USD", "PEN", new BigDecimal("100"),
                                new BigDecimal("373.92"), new BigDecimal("3.739165"), user);
                ExchangeHistory history2 = new ExchangeHistory("PEN", "USD", new BigDecimal("373.92"),
                                new BigDecimal("100"), new BigDecimal("0.267427"), user);
                List<ExchangeHistory> expectedHistory = Arrays.asList(history1, history2);

                when(userService.findById(userId)).thenReturn(Optional.of(user));
                when(exchangeHistoryRepository.findByUserIdAndDateRangeOrderByConversionDateDesc(
                                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(expectedHistory);

                // When
                List<ExchangeHistory> result = exchangeService.getExchangeHistory(userId, startDate, endDate);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
                assertEquals(expectedHistory, result);
                verify(userService).findById(userId);
                verify(exchangeHistoryRepository).findByUserIdAndDateRangeOrderByConversionDateDesc(
                                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        void testGetExchangeHistory_WithoutDateRange() {
                // Given
                Long userId = 1L;

                User user = new User("testuser", "password", "test@test.com", "Test", "User");
                user.setId(userId);

                ExchangeHistory history = new ExchangeHistory("USD", "PEN", new BigDecimal("100"),
                                new BigDecimal("373.92"), new BigDecimal("3.739165"), user);
                List<ExchangeHistory> expectedHistory = Arrays.asList(history);

                when(userService.findById(userId)).thenReturn(Optional.of(user));
                when(exchangeHistoryRepository.findByUserIdOrderByConversionDateDesc(userId))
                                .thenReturn(expectedHistory);

                // When
                List<ExchangeHistory> result = exchangeService.getExchangeHistory(userId, null, null);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(expectedHistory, result);
                verify(userService).findById(userId);
                verify(exchangeHistoryRepository).findByUserIdOrderByConversionDateDesc(userId);
        }

        @Test
        void testGetExchangeHistory_UserNotFound() {
                // Given
                Long userId = 999L;
                when(userService.findById(userId)).thenReturn(Optional.empty());

                // When & Then
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> exchangeService.getExchangeHistory(userId, null, null));

                assertTrue(exception.getMessage().contains("Usuario no encontrado"));
                verify(userService).findById(userId);
                verify(exchangeHistoryRepository, never()).findByUserIdOrderByConversionDateDesc(any());
        }
}
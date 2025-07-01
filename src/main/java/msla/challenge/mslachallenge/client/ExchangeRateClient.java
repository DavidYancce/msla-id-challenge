package msla.challenge.mslachallenge.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import msla.challenge.mslachallenge.dto.ExternalApiResponse;

@FeignClient(name = "exchange-rate-client", url = "${external.api.base-url}")
public interface ExchangeRateClient {

    @GetMapping("/convert")
    ExternalApiResponse convertCurrency(
            @RequestParam("to") String toCurrency,
            @RequestParam("from") String fromCurrency,
            @RequestParam("amount") BigDecimal amount,
            @RequestHeader("apikey") String apiKey);
}
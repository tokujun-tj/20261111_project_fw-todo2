package com.example.todoapp.api;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

public class HolidayClient {

    private static final String HOLIDAY_API_URL =
            "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient = RestClient.builder()
            .requestFactory(requestFactory())
            .build();

    private JdkClientHttpRequestFactory requestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return requestFactory;
    }

    public Map<String, String> getHolidays() {
        return getHolidaysWithStatus().holidays();
    }

    public HolidayResult getHolidaysWithStatus() {
        try {
            Map<String, String> holidays = restClient.get()
                    .uri(HOLIDAY_API_URL)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            return new HolidayResult(holidays == null ? Map.of() : holidays, false);
        } catch (RestClientException exception) {
            return new HolidayResult(Map.of(), true);
        }
    }

    public record HolidayResult(Map<String, String> holidays, boolean unavailable) {
    }
}

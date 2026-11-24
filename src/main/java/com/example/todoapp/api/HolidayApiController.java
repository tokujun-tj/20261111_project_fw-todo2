package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
public class HolidayApiController {

    private final HolidayClient holidayClient = new HolidayClient();

    @GetMapping
    public ResponseEntity<Map<String, String>> holidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        HolidayClient.HolidayResult result = holidayClient.getHolidaysWithStatus();
        Map<String, String> holidays = result.holidays().entrySet().stream()
                .filter(entry -> isInRange(entry.getKey(), from, to))
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        java.util.LinkedHashMap::new));
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.unavailable()) {
            response.header("X-Holidays-Unavailable", "true");
        }
        return response.body(holidays);
    }

    private boolean isInRange(String dateText, LocalDate from, LocalDate to) {
        LocalDate date = LocalDate.parse(dateText);
        return (from == null || !date.isBefore(from))
                && (to == null || !date.isAfter(to));
    }
}

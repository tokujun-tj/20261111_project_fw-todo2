package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {
        YearMonth now = YearMonth.now();
        YearMonth targetMonth = YearMonth.of(
                year != null ? year : now.getYear(),
                month != null ? month : now.getMonthValue());

        LocalDate firstDay = targetMonth.atDay(1);
        LocalDate lastDay = targetMonth.atEndOfMonth();
        int startOffset = firstDay.getDayOfWeek().getValue() % 7;
        int totalCells = (int) Math.ceil((startOffset + targetMonth.lengthOfMonth()) / 7.0) * 7;

        YearMonth previousMonth = targetMonth.minusMonths(1);
        YearMonth nextMonth = targetMonth.plusMonths(1);

        model.addAttribute("year", targetMonth.getYear());
        model.addAttribute("month", targetMonth.getMonthValue());
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("startOffset", startOffset);
        model.addAttribute("daysInMonth", targetMonth.lengthOfMonth());
        model.addAttribute("totalCells", totalCells);
        model.addAttribute("previousYear", previousMonth.getYear());
        model.addAttribute("previousMonth", previousMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());

        return "calendar";
    }
}

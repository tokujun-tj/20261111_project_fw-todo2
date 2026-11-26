package com.example.todoapp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final TodoService todoService;

    public DashboardController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);
        Map<String, Long> completedByDay = new HashMap<>();
        todoService.countCompletedByDay(from, today).forEach(item ->
                completedByDay.put(item.getLabel(), item.getCount()));

        List<TodoCount> dailyCounts = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(today); date = date.plusDays(1)) {
            TodoCount item = new TodoCount();
            item.setLabel(DATE_FORMAT.format(date));
            item.setCount(completedByDay.getOrDefault(item.getLabel(), 0L));
            dailyCounts.add(item);
        }

        DashboardStats stats = new DashboardStats();
        stats.setTotal(todoService.countAll());
        stats.setCompleted(todoService.countCompleted());
        stats.setIncomplete(todoService.countIncomplete());
        stats.setOverdue(todoService.countOverdue(today));
        stats.setCompletionByDay(dailyCounts);
        stats.setStatusCounts(todoService.countByStatus());
        stats.setCategoryCounts(todoService.countByCategory());
        model.addAttribute("stats", stats);
        return "dashboard";
    }
}

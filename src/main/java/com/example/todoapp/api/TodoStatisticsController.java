package com.example.todoapp.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todoapp.TodoCount;
import com.example.todoapp.TodoService;

@RestController
@RequestMapping("/api/todos/stats")
public class TodoStatisticsController {
    private static final List<String> PRIORITY_LABELS = List.of("HIGH", "MEDIUM", "LOW");
    private final TodoService todoService;

    public TodoStatisticsController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/categories")
    public List<TodoCount> categories() {
        return todoService.countByCategory();
    }

    @GetMapping("/priorities")
    public List<TodoCount> priorities() {
        return todoService.countByPriority().stream().map(this::toPriorityCount).toList();
    }

    private TodoCount toPriorityCount(TodoCount count) {
        int priority = Integer.parseInt(count.getLabel());
        if (priority < 1 || priority > PRIORITY_LABELS.size()) {
            throw new IllegalStateException("Unknown priority: " + priority);
        }
        count.setLabel(PRIORITY_LABELS.get(priority - 1));
        return count;
    }
}

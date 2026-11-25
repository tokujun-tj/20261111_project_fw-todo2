package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.example.todoapp.TodoService;
import com.example.todoapp.Todo;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(required = false, description = "キーワード") String keyword,
            @McpToolParam(required = false, description = "ジャンル") String category,
            @McpToolParam(required = false, description = "開始日（yyyy-MM-dd）") String from,
            @McpToolParam(required = false, description = "終了日（yyyy-MM-dd）") String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);

        return todoService.search(keyword, category, "asc", fromDate, toDate).stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(
            @McpToolParam(required = true, description = "やることのID") Long id) {
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(required = true, description = "やること") String title,
            @McpToolParam(required = false, description = "メモ") String detail,
            @McpToolParam(required = true, description = "ジャンル") String category,
            @McpToolParam(required = true, description = "優先度") Integer priority,
            @McpToolParam(required = false, description = "期限（yyyy-MM-dd）") String dueDate,
            @McpToolParam(required = false, description = "完了済みかどうか") Boolean completed) {
        validateCategory(category);

        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(parseDate(dueDate));
        todo.setCompleted(completed);
        todoService.create(todo);
        return TodoDto.from(todoService.findById(todo.getId()));
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(required = true, description = "やることのID") Long id,
            @McpToolParam(required = false, description = "やること") String title,
            @McpToolParam(required = false, description = "メモ") String detail,
            @McpToolParam(required = false, description = "ジャンル") String category,
            @McpToolParam(required = false, description = "優先度") Integer priority,
            @McpToolParam(required = false, description = "期限（yyyy-MM-dd）") String dueDate,
            @McpToolParam(required = false, description = "完了済みかどうか") Boolean completed) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        if (title != null) {
            todo.setTitle(title);
        }
        if (detail != null) {
            todo.setDetail(detail);
        }
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (priority != null) {
            todo.setPriority(priority);
        }
        if (dueDate != null) {
            todo.setDueDate(parseDate(dueDate));
        }
        if (completed != null) {
            todo.setCompleted(completed);
        }
        todoService.update(todo);
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(
            @McpToolParam(required = true, description = "やることのID") Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        todo.setCompleted(true);
        todoService.update(todo);
        return TodoDto.from(todoService.findById(id));
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public void deleteTodo(
            @McpToolParam(required = true, description = "やることのID") Long id) {
        todoService.delete(id);
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<String> findFreeDays(
            @McpToolParam(required = true, description = "開始日（yyyy-MM-dd）") String from,
            @McpToolParam(required = true, description = "終了日（yyyy-MM-dd）") String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        Set<LocalDate> dueDates = todoService.search(null, null, "asc", fromDate, toDate).stream()
                .map(Todo::getDueDate)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> holidays = holidayClient.getHolidaysWithStatus().holidays().keySet();

        List<String> freeDays = new java.util.ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            if (dueDates.contains(date)) {
                continue;
            }
            if (holidays.contains(date.toString())) {
                continue;
            }
            freeDays.add(date.toString());
        }
        return freeDays;
    }

    private LocalDate parseDate(String date) {
        return date == null ? null : LocalDate.parse(date);
    }

    private void validateCategory(String category) {
        Set<String> categories = Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動");
        if (!categories.contains(category)) {
            throw new IllegalArgumentException("ジャンルは指定された5種類から選んでください");
        }
    }
}

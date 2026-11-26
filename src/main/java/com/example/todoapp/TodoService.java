package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order,
            LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to);
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("Todo registration completed for id={}", todo.getId());
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
        log.info("Todo update completed for id={}", todo.getId());
    }

    public void delete(Long id) {
        todoMapper.deleteById(id);
        log.info("Todo deletion completed for id={}", id);
    }

    public List<TodoCount> countByCategory() {
        return todoMapper.countByCategory();
    }

    public List<TodoCount> countByPriority() {
        return todoMapper.countByPriority();
    }

    public long countAll() {
        return todoMapper.countAll();
    }

    public long countCompleted() {
        return todoMapper.countCompleted();
    }

    public long countIncomplete() {
        return todoMapper.countIncomplete();
    }

    public long countOverdue(LocalDate today) {
        return todoMapper.countOverdue(today);
    }

    public List<TodoCount> countCompletedByDay(LocalDate from, LocalDate to) {
        return todoMapper.countCompletedByDay(from, to);
    }

    public List<TodoCount> countByStatus() {
        return todoMapper.countByStatus();
    }
}

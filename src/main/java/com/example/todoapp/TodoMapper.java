package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {

    List<Todo> search(@Param("keyword") String keyword,
            @Param("category") String category,
            @Param("order") String order,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    Todo findById(Long id);

    void update(Todo todo);

    void insert(Todo todo);

    void deleteById(Long id);

    List<TodoCount> countByCategory();

    List<TodoCount> countByPriority();

    long countAll();

    long countCompleted();

    long countIncomplete();

    long countOverdue(LocalDate today);

    List<TodoCount> countCompletedByDay(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<TodoCount> countByStatus();
}

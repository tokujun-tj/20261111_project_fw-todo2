package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper {

    List<Todo> findAll();

    Todo findById(Long id);

    void update(Todo todo);

    void insert(Todo todo);

    void deleteById(Long id);
}

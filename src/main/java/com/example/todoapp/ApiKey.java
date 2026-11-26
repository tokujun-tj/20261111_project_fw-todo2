package com.example.todoapp;
import java.time.LocalDateTime;
import lombok.Data;
@Data
public class ApiKey { private Long id; private Long userId; private String username; private String keyHash; private String name; private LocalDateTime createdAt; private LocalDateTime lastUsedAt; private LocalDateTime revokedAt; }

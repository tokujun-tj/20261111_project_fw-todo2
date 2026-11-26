package com.example.todoapp;

import java.util.List;

import lombok.Data;

@Data
public class DashboardStats {
    private long total;
    private long completed;
    private long incomplete;
    private long overdue;
    private List<TodoCount> completionByDay;
    private List<TodoCount> statusCounts;
    private List<TodoCount> categoryCounts;
}

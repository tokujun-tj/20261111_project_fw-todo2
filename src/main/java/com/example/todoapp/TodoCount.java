package com.example.todoapp;

import lombok.Data;

/** 集計結果をMyBatisから受け取るためのモデルです。 */
@Data
public class TodoCount {
    private String label;
    private Long count;
}

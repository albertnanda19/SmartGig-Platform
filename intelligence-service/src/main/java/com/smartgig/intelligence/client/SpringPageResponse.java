package com.smartgig.intelligence.client;

import lombok.Data;

import java.util.List;

@Data
public class SpringPageResponse<T> {
    private List<T> content;
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}


package com.rutea.app.activitiesandviews.data.models.dto.common;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public List<T> getContent() {
        return content == null ? Collections.emptyList() : content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }
}

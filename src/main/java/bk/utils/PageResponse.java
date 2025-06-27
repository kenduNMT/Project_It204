package bk.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class PageResponse<T> {
    // Getters and Setters
    @Setter
    private List<T> content;
    @Setter
    private int currentPage;
    @Setter
    private int totalPages;
    @Setter
    private long totalElements;
    @Setter
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;

    public PageResponse() {
    }

    public PageResponse(List<T> content, int currentPage, int size, long totalElements) {
        this.content = content;
        this.currentPage = currentPage;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}
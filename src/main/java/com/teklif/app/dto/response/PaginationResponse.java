package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse {
    private long total;
    private int page;
    private int limit;
    private int totalPages;

    public static PaginationResponse of(long total, int page, int limit) {
        int totalPages = (int) Math.ceil((double) total / limit);
        return PaginationResponse.builder()
                .total(total)
                .page(page)
                .limit(limit)
                .totalPages(totalPages)
                .build();
    }
}
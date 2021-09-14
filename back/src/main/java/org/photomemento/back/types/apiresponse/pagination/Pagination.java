package org.photomemento.back.types.apiresponse.pagination;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Pagination {
    private Links links;
    private int page;
    private int totalPages;
    private int totalElements;
    private int pageSize;
}

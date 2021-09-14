package org.photomemento.back.types.apiresponse.pagination;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Links {
    private String first;
    private String last;
    private String previous;
    private String next;
}

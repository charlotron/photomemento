package org.photomemento.back.types.apiresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.types.apiresponse.pagination.Pagination;


/**
 * Response standard for Api calls
 *
 * @param <T>
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedApiResponse<T> extends ApiResponse<T>{
    private Pagination pagination;
}

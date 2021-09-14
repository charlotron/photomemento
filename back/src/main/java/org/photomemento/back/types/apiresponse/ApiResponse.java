package org.photomemento.back.types.apiresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.types.apiresponse.message.Message;

import java.util.List;


/**
 * Response standard for Api calls
 *
 * @param <T>
 */
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private List<Message> messages;
}

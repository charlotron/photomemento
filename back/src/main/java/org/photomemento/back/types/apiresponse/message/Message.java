package org.photomemento.back.types.apiresponse.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Object to model the error response.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    private String code;
    private String message;
    private List<Parameter> parameters;
    private MessageType type;
}

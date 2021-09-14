package org.photomemento.back.domain.entity.file.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    private String type;
    private String name;
    private String value;
}

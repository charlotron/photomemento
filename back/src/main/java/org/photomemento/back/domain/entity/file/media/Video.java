package org.photomemento.back.domain.entity.file.media;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.domain.entity.file.Media;

@SuperBuilder
@NoArgsConstructor
@Setter
@Getter
public class Video extends Media { //NOSONAR
    private Long duration;
}

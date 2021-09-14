package org.photomemento.back.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class GeoDataTotal {
    @Id
    private String name;//This is the location

    private Double latitude;
    private Double longitude;

    private Double count;
}

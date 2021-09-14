package org.photomemento.back.domain.entity.file.media.geodata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ZoomLevels {
    @AllArgsConstructor
    @Getter
    public enum ZOOM_LEVEL {
        LOW("country"), MEDIUM("state"), HIGH("city");

        private final String osmField;

        public String getField() {
            return this.name().toLowerCase() + "Level";
        }
    }

    @Indexed(sparse = true)
    private String lowLevel;
    @Indexed(sparse = true)
    private String mediumLevel;
    @Indexed(sparse = true)
    private String highLevel;

    public String getZoomLevel(ZOOM_LEVEL level) {
        switch (level) {
            case LOW:
                return getLowLevel();
            case MEDIUM:
                return getMediumLevel();
            case HIGH:
                return getHighLevel();
        }
        return null;
    }
}

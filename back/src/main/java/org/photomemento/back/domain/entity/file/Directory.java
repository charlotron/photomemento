package org.photomemento.back.domain.entity.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.domain.entity.File;
import org.photomemento.back.domain.entity.HierarchyDir;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Getter
@Document("directories")
@CompoundIndex(name="findByParentHashOrderByPathAsc", def="{'parentHash': 1, 'path': 1}")
public class Directory extends File {

    @Override
    public FILE_TYPE getType() {
        return FILE_TYPE.DIRECTORY;
    }

    protected List<HierarchyDir> hierarchy;
}

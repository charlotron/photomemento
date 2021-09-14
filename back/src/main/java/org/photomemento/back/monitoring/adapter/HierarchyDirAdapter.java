package org.photomemento.back.monitoring.adapter;

import org.photomemento.back.domain.entity.HierarchyDir;
import org.photomemento.back.domain.entity.file.Directory;
import org.springframework.stereotype.Component;

@Component
public class HierarchyDirAdapter {
    public HierarchyDir fromDir(Directory dir){
        if(dir==null) return null;
        return HierarchyDir.builder()
                .id(dir.getId())
                .name(dir.getName())
                .build();
    }
}

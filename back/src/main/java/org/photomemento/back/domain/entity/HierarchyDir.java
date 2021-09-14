package org.photomemento.back.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HierarchyDir {
    protected String id;
    protected String name;
}

package org.photomemento.back.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.photomemento.back.types.enu.FILE_TYPE;
import org.photomemento.back.types.enu.STATUS_STEP;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuperBuilder
@NoArgsConstructor
@Setter
@Getter
@Document("files")
//TextIndexed fields are on MongoConfig class
public abstract class File implements Comparable<File> {
    @Id
    protected String id;
    @Indexed
    protected String parent;
    @Indexed
    protected String parentHash;
    @Indexed
    protected String path;
    protected String name;

    protected Map<String, STATUS_STEP> status;

    public abstract FILE_TYPE getType();

    @Override
    public int compareTo(File o) {
        if (o == null) return 1;
        if (o == this) return 0;
        return this.path.compareTo(o.getPath());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof File)) return false;
        return compareTo((File) o) == 0;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Transient
    @JsonIgnore
    public java.io.File getFile() {
        return new java.io.File(getPath());
    }

    public void setStatusValue(String key, STATUS_STEP value) {
        if (this.status == null) this.status = new HashMap<>();
        status.put(key, value);
    }

    public STATUS_STEP getStatusValue(String key) {
        return Optional.ofNullable(this.status).map(map -> map.get(key)).orElse(null);
    }
}

package org.photomemento.back.types.initializer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InitializerList<K, T extends List<K>> extends I<T> {
    public InitializerList(T data) {
        super(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<K> stream(){
        return Optional.ofNullable(data)
                .map(Collection::stream)
                .orElse(Stream.ofNullable(null));
    }
}

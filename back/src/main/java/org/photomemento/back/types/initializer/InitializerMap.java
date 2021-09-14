package org.photomemento.back.types.initializer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class InitializerMap<K, V, T extends Map<K, V>> extends I<T> {
    public InitializerMap(T data) {
        super(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<Map.Entry<K, V>> stream(){
        return Optional.ofNullable(data)
                .map(Map::entrySet)
                .map(Collection::stream)
                .orElse(Stream.ofNullable(null));
    }
}

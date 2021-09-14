package org.photomemento.back.types.initializer;

import java.util.stream.Stream;

/**
 * Inline initializer, a helper class for performing similar ops than Optional<T>
 *
 * @param <T> the class to be operated
 */
public class InitializerObject<T> extends I<T> {

    InitializerObject(T data) {
        super(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected InitializerObject<T> getInstance(){
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<T> stream(){
        return Stream.ofNullable(data);
    }
}

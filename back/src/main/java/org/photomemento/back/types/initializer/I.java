package org.photomemento.back.types.initializer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Inline initializer, a helper class for performing similar ops than Optional<T>
 *
 * @param <T> the class to be operated
 */
public abstract class I<T> {
    protected final T data;

    I(T data) {
        this.data = data;
    }

    public T get() {
        return data;
    }

    public boolean isPresent() {
        return Optional.ofNullable(data).isPresent();
    }

    public boolean isEmpty() {
        return Optional.ofNullable(data).isEmpty();
    }

    public T orElse(T ifEmpty) {
        return Optional.ofNullable(data).orElse(ifEmpty);
    }

    public <Z extends I<T>> Z alwaysTap(ReturnVoidProcessor<T> processor) {
        processor.process(data);
        return getInstance();
    }

    @SuppressWarnings("unchecked")
    protected <Z extends I<T>> Z getInstance() {
        return (Z) this;
    }

    public <Z extends I<T>> Z tap(ReturnVoidProcessor<T> processor) {
        if (Optional.ofNullable(data).isPresent())
            processor.process(data);
        return getInstance();
    }

    public <Z extends I<T>> Z nullTap(ReturnVoidNullProcessor processor) {
        if (Optional.ofNullable(data).isEmpty())
            processor.process();
        return getInstance();
    }

    @SuppressWarnings("unchecked")
    public <Y,Z extends I<Y>> Z map(ReturnInlineInitializerProcessor<T, Y> processor) {
        return (Z) of(Optional.ofNullable(data)
                .map(processor::process)
                .orElse(null));
    }

    @SuppressWarnings("unchecked")
    public <Z extends I<T>> Z filter(ReturnBoolProcessor<T> processor) {
        return (Z) I.of(Optional.ofNullable(data)
                .filter(processor::process)
                .orElse(null));
    }

    public T tapGet(ReturnVoidProcessor<T> processor) {
        return tap(processor)
                .get();
    }

    public T nullTapGet(ReturnVoidNullProcessor processor) {
        return nullTap(processor)
                .get();
    }

    public <Z> Z mapGet(ReturnInlineInitializerProcessor<T, Z> processor) {
        return map(processor)
                .get();
    }

    public T filterGet(ReturnBoolProcessor<T> processor) {
        return filter(processor)
                .get();
    }

    public abstract <K, V extends Stream<K>> V stream();

    public static <K, V, U extends Map<K, V>> InitializerMap<K, V, U> of(U data) {
        return new InitializerMap<>(data);
    }

    public static <K, U extends List<K>> InitializerList<K, U> of(U data) {
        return new InitializerList<>(data);
    }

    public static <U> InitializerObject<U> of(U data) {
        return new InitializerObject<>(data);
    }

    public static <T> T ofFilterGet(T data, ReturnBoolProcessor<T> processor) {
        return of(data)
                .filterGet(processor);
    }

    public static <T> T ofTapGet(T data, ReturnVoidProcessor<T> processor) {
        return of(data)
                .tapGet(processor);
    }

    public static <T, Z> Z ofMapGet(T data, ReturnInlineInitializerProcessor<T, Z> processor) {
        return of(data)
                .mapGet(processor);
    }

    @FunctionalInterface
    public interface ReturnVoidProcessor<T> {
        void process(T data);
    }

    @FunctionalInterface
    public interface ReturnBoolProcessor<T> {
        boolean process(T data);
    }

    @FunctionalInterface
    public interface ReturnVoidNullProcessor {
        void process();
    }

    @FunctionalInterface
    public interface ReturnInlineInitializerProcessor<T, Z> {
        Z process(T data);
    }

    public static void main(String... args) {
        I.of(List.of("oli"))
                .stream()
                .forEach(entry -> System.out.printf("res %s", entry));
    }
}

package pl.edu.wat.student.rzepinski.jakub.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IntPairs {
    private final int startInclusive;
    private final int endExclusive;

    private IntPairs(int startInclusive, int endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    public static IntPairs range(int startInclusive, int endExclusive) {
        return new IntPairs(startInclusive, endExclusive);
    }

    public void forEach(BiConsumer<Integer, Integer> consumer) {
        IntStream.range(startInclusive, endExclusive).forEach(x ->
                IntStream.range(startInclusive, endExclusive).forEach(y ->
                        consumer.accept(x, y)
                )
        );
    }

    public <R> Stream<R> map(BiFunction<Integer, Integer, R> function) {
        return IntStream.range(startInclusive, endExclusive).boxed().flatMap(x ->
                IntStream.range(startInclusive, endExclusive).mapToObj(y ->
                        function.apply(x, y)
                )
        );
    }

}

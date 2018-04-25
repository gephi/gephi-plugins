package pl.edu.wat.student.rzepinski.jakub;

import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public class Utils {
    public static void doubleIntStream(int startInclusive, int endExclusive, BiConsumer<Integer, Integer> consumer) {
        IntStream.range(startInclusive, endExclusive).forEach(x ->
                IntStream.range(startInclusive, endExclusive).forEach(y ->
                        consumer.accept(x, y)
                )
        );
    }
}

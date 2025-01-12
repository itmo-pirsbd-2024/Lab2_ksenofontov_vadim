package org.example.bench;

import org.example.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.Random;

import static java.util.Arrays.sort;

@BenchmarkMode(Mode.AverageTime)
//@BenchmarkMode(Mode.SampleTime)

//@Fork(value = 1, jvmArgs = "-Xss100m -Xmx3g")
@Fork(value = 1)
@Warmup(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.MILLISECONDS)
//@Timeout(time = 0)


@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class SortPerformanceTest {

    //@Param({"100"})
    @Param({"1000", "2000", "4000", "8000", "16000", "32000", "64000", "128000"})
    private int NUMBER_OF_ELEMENTS;

    int[] ARRAY;

    @Setup
    public void setup() {
        Random random = new Random();
        random.setSeed(0);
        //var random = ThreadLocalRandom.current();
        ARRAY = IntStream.generate(() -> random.nextInt(NUMBER_OF_ELEMENTS * 5)).limit(NUMBER_OF_ELEMENTS).toArray();
    }

    //---------------------------------------------------------------------------
    @Benchmark
    public void bubbleSortTest(Blackhole blackhole)
    {
        if (NUMBER_OF_ELEMENTS <= 10_000) {

            var array_copy = ARRAY.clone();

            Main.bubbleSort(array_copy);

            blackhole.consume(array_copy);
        }
    }

    //---------------------------------------------------------------------------
    @Benchmark
    public void QSortRecursiveTest(Blackhole blackhole)
    {
        var array_copy = ARRAY.clone();

        Main.QSortRecursive(array_copy);

        blackhole.consume(array_copy);
    }

    //---------------------------------------------------------------------------
    @Benchmark
    public void QSortIterativeTest(Blackhole blackhole)
    {
        var array_copy = ARRAY.clone();

        Main.QSortIterative(array_copy);

        blackhole.consume(array_copy);
    }

    //---------------------------------------------------------------------------
    @Benchmark
    public void MergeSortTest(Blackhole blackhole)
    {
        var array_copy = ARRAY.clone();

        Main.MergeSort(array_copy);

        blackhole.consume(array_copy);
    }

    //---------------------------------------------------------------------------
    @Benchmark
    public void BuiltinSortTest(Blackhole blackhole)
    {
        var array_copy = ARRAY.clone();

        sort(array_copy);

        blackhole.consume(array_copy);
    }
}
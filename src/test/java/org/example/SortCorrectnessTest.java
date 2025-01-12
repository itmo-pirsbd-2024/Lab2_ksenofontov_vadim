package org.example;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.util.Arrays.sort;

class SortCorrectnessTest {

    int[] generateArray(int numberOfElements) {
        Random random = new Random();
        random.setSeed(0);
        return IntStream.generate(() -> random.nextInt(numberOfElements * 5)).limit(numberOfElements).toArray();
    }

    boolean isArraySorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }

        return true;
    }

    @Test
    void bubbleSortTest() {

        var numberOfElements = 10_000;

        int[] array = generateArray(numberOfElements);

        if (numberOfElements <= 100) {
            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        Main.bubbleSort(array);

        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        assert(isArraySorted(array));
    }

    @Test
    void QSortRecursiveTest() {

        var numberOfElements = 50_000_000;

        int[] array = generateArray(numberOfElements);

        if (numberOfElements <= 100) {
            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        System.out.println(heapSize);
        System.out.println(heapMaxSize);
        System.out.println(heapFreeSize);

        Main.QSortRecursive(array);

        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        assert(isArraySorted(array));
    }

    @Test
    void QSortIterativeTest() {

        var numberOfElements = 50_000_000;

        int[] array = generateArray(numberOfElements);

        if (numberOfElements <= 100) {
            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        Main.QSortIterative(array);

        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        assert(isArraySorted(array));
    }

    @Test
    void MergeSortTest() {

        var numberOfElements = 50_000_000;

        int[] array = generateArray(numberOfElements);

        if (numberOfElements <= 100) {
            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        Main.MergeSort(array);

        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        assert(isArraySorted(array));
    }

    @Test
    void BuiltinSortTest() {

        var numberOfElements = 50_000_000;

        int[] array = generateArray(numberOfElements);

        if (numberOfElements <= 100) {
            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        sort(array);

        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }

        assert(isArraySorted(array));
    }
}
package org.example;


import java.util.ArrayDeque;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;
import static java.util.Arrays.sort;

public class Main {

    public static int[] generateArray(int numberOfElements) {
        var random = ThreadLocalRandom.current();
        return IntStream.generate(() -> random.nextInt(numberOfElements * 5)).limit(numberOfElements).toArray();
    }

    //--------------------------------------------------------------------------------
    public static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 1; j < (arr.length); j++) {
                if (arr[j - 1] > arr[j]){
                    int temp = arr[j - 1];
                    arr[j - 1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
    }

    //--------------------------------------------------------------------------------
    private static void QSortRecImpl(int[] arr, int beginIndex, int endIndex) {
        if (endIndex - beginIndex >= 2) {
            var valueToCompareWith = arr[beginIndex];

            var l = beginIndex + 1;
            var r = endIndex - 1;

            while (l < r) {
                while ((arr[l] < valueToCompareWith) && (l < endIndex - 1))
                    l++;

                while ((arr[r] >= valueToCompareWith) && (beginIndex + 1 < r))
                    r--;

                if(l < r){
                    var tmp = arr[l];
                    arr[l] = arr[r];
                    arr[r] = tmp;
                }
            }

            if (arr[beginIndex] > arr[l]) {
                var tmp = arr[beginIndex];
                arr[beginIndex] = arr[l];
                arr[l] = tmp;
            }

            QSortRecImpl(arr, beginIndex, l);
            QSortRecImpl(arr, r, endIndex);
        }
    }

    public static void QSortRecursive(int[] arr) {

        QSortRecImpl(arr, 0, arr.length);
    }

    //--------------------------------------------------------------------------------
    private static int IterPartition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left;
        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
            }
        }

        int temp = arr[i];
        arr[i] = arr[right];
        arr[right] = temp;
        return i;
    }

    public static void QSortIterative(int[] arr) {

//        Stack<Integer> stack = new Stack<>();
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        var l = 0;
        var r = arr.length - 1;

        stack.push(l);
        stack.push(r);


        while (!stack.isEmpty()) {
            r = stack.pop();
            l = stack.pop();

            int pivotIndex = IterPartition(arr, l, r);

            if (pivotIndex - 1 > l) {
                stack.push(l);
                stack.push(pivotIndex - 1);
            }

            if (pivotIndex + 1 < r) {
                stack.push(pivotIndex + 1);
                stack.push(r);
            }
        }
    }

    //--------------------------------------------------------------------------------
    private static void MergeSubArrays(int[] arr, int l, int mid, int r) {
        int[] merged = new int [r - l + 1];

        var mid_copy = mid;
        var l_copy = l;

        mid++;

        for (int i = 0; i < merged.length; i++) {

            if (l > mid_copy) {
                merged[i] = arr[mid];
                mid++;
            } else if (mid > r) {
                merged[i] = arr[l];
                l++;
            } else if (arr[l] < arr[mid]) {
                merged[i] = arr[l];
                l++;
            } else {
                merged[i] = arr[mid];
                mid++;
            }

        }

        for (int i = 0; i < merged.length; i++) {
            arr[l_copy + i] = merged[i];
        }
    }

    private static void MergeSortImpl(int[] arr, int l, int r) {
        if (l < r){
            var mid = (l + r) / 2;

            MergeSortImpl(arr, l, mid);
            MergeSortImpl(arr, mid + 1, r);
            MergeSubArrays(arr, l, mid, r);

        }
    }

    public static void MergeSort(int[] arr) {
        MergeSortImpl(arr, 0, arr.length - 1);
    }
    //--------------------------------------------------------------------------------

    public static void main(String[] args) {

        // for measurements
        var numberOfElements = 100_000;

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

        long pid = ProcessHandle.current().pid();
        System.out.println("pid: ");
        System.out.println(pid);
        try {
            TimeUnit.MILLISECONDS.sleep(10_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long start = System.nanoTime();

        Main.MergeSort(array);

        long end = System.nanoTime();

        System.out.println ("1 Time Difference: " + (end - start) / 1_000_000);


        //--------------------------------
        /*
        start = System.nanoTime();

//        sort(array2);
//        Main.bubbleSort(array2);
        Main.QSortIterative(array2);

        end = System.nanoTime();

        System.out.println ("2 Time Difference: " + (end - start) / 1_000_000);


        //--------------------------------
        start = System.nanoTime();

        Main.MergeSort(array3);

        end = System.nanoTime();

        System.out.println ("3 Time Difference: " + (end - start) / 1_000_000);


        for (int i = 0; i < numberOfElements; i++) {
            if ((array[i] != array2[i]) || (array2[i] != array3[i]))
                System.out.println ("Not equal: " + array[i] + " " + array2[i] + " " + array3[i]);
        }


        if (numberOfElements <= 100) {
            System.out.println("\n\nSorted:");

            for (int j : array) {
                System.out.print(" " + j);
            }
        }*/


    }
}
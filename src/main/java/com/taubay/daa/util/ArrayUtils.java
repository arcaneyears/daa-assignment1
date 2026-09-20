package com.taubay.daa.util;

import java.util.Random;

/** Array generators and small helpers shared by the benchmark and the tests. */
public final class ArrayUtils {

    private ArrayUtils() {
    }

    /** Input distributions used by the benchmark. */
    public enum InputType {
        /** Uniformly random ints over the whole int range. */
        RANDOM("random"),
        /** Already sorted ascending — the classic QuickSort worst case for a fixed pivot. */
        SORTED("sorted"),
        /** Sorted descending. */
        REVERSED("reversed"),
        /** Random values from 0..9, so almost every element has many equal siblings. */
        DUPLICATES("duplicates");

        private final String label;

        InputType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static int[] generate(InputType type, int n, Random rnd) {
        return switch (type) {
            case RANDOM -> randomArray(n, rnd);
            case SORTED -> sortedArray(n);
            case REVERSED -> reversedArray(n);
            case DUPLICATES -> duplicatesArray(n, rnd);
        };
    }

    public static int[] randomArray(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt();
        }
        return a;
    }

    public static int[] randomArray(int n, int bound, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(bound);
        }
        return a;
    }

    public static int[] sortedArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }
        return a;
    }

    public static int[] reversedArray(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = n - i;
        }
        return a;
    }

    /** Random values in 0..9 — stresses the 3-way partition. */
    public static int[] duplicatesArray(int n, Random rnd) {
        return randomArray(n, 10, rnd);
    }

    public static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }

    public static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}

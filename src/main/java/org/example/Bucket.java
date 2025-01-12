package org.example;

import java.util.LinkedList;

public class Bucket {
//    static int bucketSize = (4096 - 1 * 8) / 8;
    static int bucketSize = (512 - 1 * 8) / 8;
//    static int bucketSize = 3;

    int localDepth = 1;
    LinkedList<Long> values = new LinkedList<>();
}

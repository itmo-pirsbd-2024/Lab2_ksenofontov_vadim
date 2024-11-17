package org.example;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.Collections.sort;
import java.util.*;


public class Main {
    // ------------------------------------------------------
    static void shuffleArray(long[] ar, long seed) {
        Random rnd = new Random();
        rnd.setSeed(seed);
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);

            long a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    // ------------------------------------------------------
    // Generate shuffle of random sequence
    static void generateShuffleOfRandomSequence() {
        var NUMBER_OF_ELEMENTS = 25;
        var NUMBER_OF_ELEMENTS_FOR_ONE_SEED = 5;
        // NUMBER_OF_ELEMENTS % NUMBER_OF_ELEMENTS_FOR_ONE_SEED should be 0

        Random random = new Random();
        random.setSeed(0);
        var seeds = LongStream.generate(() -> random.nextLong(Long.MAX_VALUE)).limit((NUMBER_OF_ELEMENTS + NUMBER_OF_ELEMENTS_FOR_ONE_SEED - 1)/ NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();

        for (int i = 0; i < seeds.length; i++)
        {
            System.out.print(seeds[i] + " ");
        }
        System.out.println();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
                System.out.print(" | ");
            }

            var val = random.nextLong(100);

            System.out.print(val + " ");
        }
        System.out.println();

        shuffleArray(seeds, 4);

        for (int i = 0; i < seeds.length; i++)
        {
            System.out.print(seeds[i] + " ");
        }
        System.out.println();

        long[] valuesForCurrentSeed = new long[NUMBER_OF_ELEMENTS_FOR_ONE_SEED];
        int indexCounter = 0;
        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
                System.out.print(" | ");
                valuesForCurrentSeed = LongStream.generate(() -> random.nextLong(100)).limit(NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();
                shuffleArray(valuesForCurrentSeed, i);
                indexCounter = 0;
            }

            var val = valuesForCurrentSeed[indexCounter++];

            System.out.print(val + " ");
        }
        System.out.println();
    }

    // Write numbers on disk -> read numbers from disk
    static void diskWriteReadOfNumbers() throws IOException {
        RandomAccessFile fileObject = new RandomAccessFile("file.bin", "rw");

        Random random = new Random();
        random.setSeed(0);

        var NUMBER_OF_ELEMENTS_IN_ITERATION = 2_000_000;
        var NUMBER_OF_ITERATIONS = 100;
        var array = LongStream.generate(() -> random.nextLong(100_000 * 5L)).limit(NUMBER_OF_ELEMENTS_IN_ITERATION).toArray();
        long timeDelta = 0;
        long start = 0;
        long end = 0;
        long sum = 0;

        byte[] bytes = new byte[NUMBER_OF_ELEMENTS_IN_ITERATION * 8];

        var buffer = ByteBuffer.wrap(bytes).asLongBuffer();

        for (int i = 0; i < NUMBER_OF_ELEMENTS_IN_ITERATION; i++) {
            buffer.put(array[i]);
        }

        start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            fileObject.write(bytes);

            int index1 = random.nextInt(NUMBER_OF_ELEMENTS_IN_ITERATION * 8);
            int index2 = random.nextInt(NUMBER_OF_ELEMENTS_IN_ITERATION * 8);

            byte a = bytes[index1];
            bytes[index1] = bytes[index2];
            bytes[index2] = a;
        }

        end = System.nanoTime();
        timeDelta = (end - start);

        System.out.println ("Batch Write time delta: " + (timeDelta) / 1_000_000 + " ms, " + NUMBER_OF_ELEMENTS_IN_ITERATION * 8L * NUMBER_OF_ITERATIONS / 1024 +
                " Kb, Speed: " + NUMBER_OF_ELEMENTS_IN_ITERATION * 8L * NUMBER_OF_ITERATIONS / ((timeDelta) / 1000 ) + "Mb/s");

        /*fileObject.setLength(0);

        start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            fileObject.writeLong(array[i]);
        }

        end = System.nanoTime();
        timeDelta = (end - start);

        System.out.println ("Seq Write Time Difference: " + (timeDelta) / 1_000_000 + " ms, " + NUMBER_OF_ELEMENTS * 8 / 1024 +
                " Kb, Speed: " + NUMBER_OF_ELEMENTS * 8 / ((timeDelta) / 1000 ) + "Mb/s");

         */

        fileObject.seek(0);

        start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            fileObject.seek((i) * NUMBER_OF_ELEMENTS_IN_ITERATION * 8L);
            sum+=fileObject.read(bytes, 0, NUMBER_OF_ELEMENTS_IN_ITERATION * 8);
        }

        end = System.nanoTime();
        timeDelta = (end - start);

        System.out.println ("Batch Read time delta: " + (timeDelta) / 1_000_000 + " ms, " + NUMBER_OF_ELEMENTS_IN_ITERATION * 8L * NUMBER_OF_ITERATIONS / 1024 +
                " Kb, Speed: " + NUMBER_OF_ELEMENTS_IN_ITERATION * 8L * NUMBER_OF_ITERATIONS / ((timeDelta) / 1000 ) + "Mb/s");
        System.out.println(sum);

        var r_buffer = ByteBuffer.wrap(bytes).asLongBuffer();

        var val = 0L;
        for (int j = 0; j < NUMBER_OF_ELEMENTS_IN_ITERATION; j++) {
            val = r_buffer.get();
            array[j] = val;
        }

        fileObject.setLength(0);
        fileObject.close();
    }

    // Generate Buckets -> Write on disk -> read from disk
    static void generateBucketsWriteRead() throws IOException {
        Random random = new Random();
        //random.setSeed(0);

        var NUMBER_OF_BUCKETS = 1_000;

        ArrayList<Bucket> buckets = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
            Bucket bucket = new Bucket();
            for (int j = 0; j < Bucket.bucketSize; j++) {
                bucket.values.add(random.nextLong(100_000 * 5L));
            }

            buckets.add(bucket);
        }

        OndiskBucketManager mngr = new OndiskBucketManager("file.bin");

        long timeDelta = 0;
        long start = 0;
        long end = 0;

        start = System.nanoTime();

        for (int i = 0; i < buckets.size(); i++) {
//            start = System.nanoTime();
            mngr.writeBucketToDisk(buckets.size() - 1 - i, buckets.get(i));
//            mngr.writeBucketToDisk(i, buckets.get(i));
            //          end = System.nanoTime();
            //        timeDelta += (end - start);
        }

        end = System.nanoTime();
        timeDelta = (end - start);

        System.out.println("Write time delta: " + (timeDelta) / 1_000_000 + " ms, " + "Just write: " + (mngr.writeTimeDelta) / 1_000_000 + " ms, " + NUMBER_OF_BUCKETS * Bucket.bucketSize * 8 / 1024 +
                " Kb, Speed: " + NUMBER_OF_BUCKETS * Bucket.bucketSize * 8 / ((timeDelta) / 1000) + "Mb/s");

        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ArrayList<Bucket> r_buckets = new ArrayList<>();

        timeDelta = 0;

        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
            start = System.nanoTime();
            var r_bucket = mngr.readBucketFromDisk(NUMBER_OF_BUCKETS - 1 - i);
//            var r_bucket = mngr.readBucketFromDisk(i);
            end = System.nanoTime();

            r_buckets.add(r_bucket);
            timeDelta += (end - start);
        }

        System.out.println("Read time delta: " + (timeDelta) / 1_000_000 + " ms, " + "Just read: " + (mngr.readTimeDelta) / 1_000_000 + " ms, " + NUMBER_OF_BUCKETS * Bucket.bucketSize * 8 / 1024 +
                " Kb, Speed: " + NUMBER_OF_BUCKETS * Bucket.bucketSize * 8 / ((timeDelta) / 1000) + "Mb/s");

        for (int i = 0; i < NUMBER_OF_BUCKETS; i++) {
            Bucket bucket = buckets.get(i);
            Bucket r_bucket = r_buckets.get(i);

            if (bucket.values.size() != r_bucket.values.size()) {
                System.out.println("sizes of original and restored buckets " + i + " is different");
                continue;
            }

            var bucketArr = bucket.values.toArray();
            var r_bucketArr = r_bucket.values.toArray();

            for (int j = 0; j < bucketArr.length; j++) {
                if (bucketArr[j].equals(r_bucketArr[j]) == false) {
                    System.out.println("values of original and restored buckets " + i + " in position " + j + " are different: " + bucketArr[j] + " and " + r_bucketArr[j]);
                }
            }

            buckets.add(bucket);
        }

        mngr.clear();
    }

    // Hashtable filling example
    static void hashtableFillingExample() throws IOException {
        FractionalHasher hasher = new FractionalHasher();

        InmemoryBucketManager mngr = new InmemoryBucketManager();
        ExtendibleHashTable hshtbl = new ExtendibleHashTable(mngr, hasher);

        hshtbl.insertValue(3);
        hshtbl.insertValue(13);
        hshtbl.insertValue(4);
        hshtbl.insertValue(109);
        hshtbl.insertValue(32);
        hshtbl.insertValue(25);
        hshtbl.insertValue(112);

        System.out.println(hshtbl.valueExists(3));
        System.out.println(hshtbl.valueExists(13));
        System.out.println(hshtbl.valueExists(4));
        System.out.println(hshtbl.valueExists(109));
        System.out.println(hshtbl.valueExists(32));
        System.out.println(hshtbl.valueExists(25));
        System.out.println(hshtbl.valueExists(112));

        System.out.println(hshtbl.valueExists(1212));
        System.out.println(hshtbl.valueExists(1123));


        hshtbl.deleteValue(25);
        hshtbl.deleteValue(109);
        hshtbl.deleteValue(3);
        hshtbl.deleteValue(32);
        hshtbl.deleteValue(3);
        hshtbl.deleteValue(13);
        hshtbl.deleteValue(4);
        hshtbl.deleteValue(112);


        System.out.println("counter: " + hshtbl.counterOfMovings);
    }

    // Generate HashTable -> Find Elements -> Delete elements
    static void generateHashTableFindElementsDeleteElements() throws IOException {
        Random random = new Random();
        random.setSeed(0);

        var NUMBER_OF_ELEMENTS = 10_000_000;
        var MAX_VALUE_FOR_GENERATING = 10_000_000_000L;
        //var MAX_VALUE_FOR_GENERATING = Long.MAX_VALUE;
        //var MAX_VALUE_FOR_GENERATING = NUMBER_OF_ELEMENTS * 5L;


//        FractionalHasher hasher = new FractionalHasher();
//        GuavaHasher hasher = new GuavaHasher();
        UniversalHasher hasher = new UniversalHasher();
        OndiskBucketManager mngr = new OndiskBucketManager("file.bin");
//        InmemoryBucketManager mngr = new InmemoryBucketManager();

        ExtendibleHashTable2 hshtbl = new ExtendibleHashTable2(mngr, hasher);

//        ArrayList<Integer> hashes = new ArrayList<>();

        long start = System.nanoTime();

        for (var j = 0; j < NUMBER_OF_ELEMENTS; j++) {
            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);
//            hashes.add(hasher.generateHash(val));
            hshtbl.insertValue(val);
        }

        long end = System.nanoTime();

        System.out.println ("Time of insertion: " + (end - start) / 1_000_000 + " ms, " + (hshtbl.maxBucketId + 1) * Bucket.bucketSize * 8 / 1024 +
                " Kb, Speed from the perspective of elements number: " + ((double) NUMBER_OF_ELEMENTS) / (((double)(end - start)) / 1_000_000 ) + " el/ms");
        System.out.println ("Speed from the perspective of summary bucket size: " + ((double) hshtbl.maxBucketId + 1) * Bucket.bucketSize * 8 / ((end - start) / 1000 ) + " Mb/s");
        System.out.println ("Write size: " + ((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings * (Bucket.bucketSize + 1) * 8 / 1024 +
                " Kb, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / ((end - start) / 1000 ) + " Mb/s");
        System.out.println ("Read size: " + ((OndiskBucketManager)hshtbl.bucketManager).counterOfReadings * (Bucket.bucketSize + 1) * 8 / 1024 +
                " Kb, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfReadings)) * (Bucket.bucketSize + 1) * 8 / ((end - start) / 1000 ) + " Mb/s");

        System.out.println("size of directories: " + hshtbl.directories.size());
        System.out.println("counterOfMovings: " + hshtbl.counterOfMovings);
        System.out.println("maxLocalDepth: " + hshtbl.maxLocalDepth);
        System.out.println("maxBucketId: " + hshtbl.maxBucketId);
        System.out.println("average bucket filling: " + (double) NUMBER_OF_ELEMENTS / ((hshtbl.maxBucketId + 1) * Bucket.bucketSize));
        System.out.println("size of highCollisedValues: " + hshtbl.highCollisedValues.size());
        System.out.println("mngr.writeTimeDelta: " + mngr.writeTimeDelta / 1000_000 + " ms, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / (mngr.writeTimeDelta / 1000) + " Mb/s");
        System.out.println("mngr.readTimeDelta: " + mngr.readTimeDelta / 1000_000 + " ms, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / (mngr.readTimeDelta / 1000) + " Mb/s");
        System.out.println("mngr.cachePriorityQueueTimeDelta: " + mngr.cachePriorityQueueTimeDelta / 1000_000 + " ms");

        System.out.println ("<============================================>\n");

        random = new Random();
        random.setSeed(0);

        start = System.nanoTime();

        for (var j = 0; j < NUMBER_OF_ELEMENTS; j++) {
            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);
            if (hshtbl.valueExists(val) == false) {
                System.out.println ("Value " + val + " inserted but not found(");
            }
        }

        end = System.nanoTime();

        System.out.println ("Time of finding of all inserted values: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");

        System.out.println ("\n<============================================>\n");

        random = new Random();
        random.setSeed(0);

        start = System.nanoTime();

        for (var j = 0; j < NUMBER_OF_ELEMENTS; j++) {
            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);
            hshtbl.deleteValue(val);
        }

        end = System.nanoTime();

        System.out.println ("Time of deleting of all inserted values: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");

        System.out.println ("\n<============================================>\n");

        random = new Random();
        random.setSeed(0);

        start = System.nanoTime();

        for (var j = 0; j < NUMBER_OF_ELEMENTS; j++) {
            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);
            if (hshtbl.valueExists(val) == true) {
                System.out.println ("Value " + val + " deleted but found(");
            }
        }

        end = System.nanoTime();

        System.out.println ("Time of checking table for emptiness: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");

        hshtbl.bucketManager.clear();

        /*sort(hashes);

        ArrayList<Integer> bitstat = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            bitstat.add(0);
        }

        for (int j : hashes) {
            System.out.println(Integer.toBinaryString(j));

            for (int i = 0; i < 31; i++) {
                var curval = bitstat.get(i);
                curval += j % 2;
                bitstat.set(i, curval);

                j /= 2;
            }
        }

        for (int i = 0; i < 31; i++) {
            System.out.println(i + ": " + ((double) bitstat.get(i) / hashes.size()));
        }*/
    }

    // Generate HashTable -> Find Elements (shuffled) -> Delete elements (shuffled)
    static void generateHashTableFindShuffledElementsDeleteShuffledElements() throws IOException {
        Random random = new Random();
        random.setSeed(0);

        var NUMBER_OF_ELEMENTS = 2_00_000;
        var MAX_VALUE_FOR_GENERATING = 10_000_000_000L;
        //var MAX_VALUE_FOR_GENERATING = Long.MAX_VALUE;
        //var MAX_VALUE_FOR_GENERATING = NUMBER_OF_ELEMENTS * 5L;

        var NUMBER_OF_ELEMENTS_FOR_ONE_SEED = 100;
        // NUMBER_OF_ELEMENTS % NUMBER_OF_ELEMENTS_FOR_ONE_SEED should be 0

//        FractionalHasher hasher = new FractionalHasher();
//        GuavaHasher hasher = new GuavaHasher();
        UniversalHasher hasher = new UniversalHasher();
        OndiskBucketManager mngr = new OndiskBucketManager("file.bin");
//        InmemoryBucketManager mngr = new InmemoryBucketManager();

        ExtendibleHashTable2 hshtbl = new ExtendibleHashTable2(mngr, hasher);

        var seeds = LongStream.generate(() -> random.nextLong(Long.MAX_VALUE)).limit((NUMBER_OF_ELEMENTS + NUMBER_OF_ELEMENTS_FOR_ONE_SEED - 1)/ NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();

        long start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
            }

            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);
            hshtbl.insertValue(val);
        }

        long end = System.nanoTime();

        System.out.println ("Time of insertion: " + (end - start) / 1_000_000 + " ms, " + (hshtbl.maxBucketId + 1) * Bucket.bucketSize * 8 / 1024 +
                " Kb, Speed from the perspective of elements number: " + ((double) NUMBER_OF_ELEMENTS) / (((double)(end - start)) / 1_000_000 ) + " el/ms");
        System.out.println ("Speed from the perspective of summary bucket size: " + ((double) hshtbl.maxBucketId + 1) * Bucket.bucketSize * 8 / ((end - start) / 1000 ) + " Mb/s");
        System.out.println ("Write size: " + ((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings * (Bucket.bucketSize + 1) * 8 / 1024 +
                " Kb, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / ((end - start) / 1000 ) + " Mb/s");
        System.out.println ("Read size: " + ((OndiskBucketManager)hshtbl.bucketManager).counterOfReadings * (Bucket.bucketSize + 1) * 8 / 1024 +
                " Kb, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfReadings)) * (Bucket.bucketSize + 1) * 8 / ((end - start) / 1000 ) + " Mb/s");

        System.out.println("size of directories: " + hshtbl.directories.size());
        System.out.println("counterOfMovings: " + hshtbl.counterOfMovings);
        System.out.println("maxLocalDepth: " + hshtbl.maxLocalDepth);
        System.out.println("maxBucketId: " + hshtbl.maxBucketId);
        System.out.println("average bucket filling: " + (double) NUMBER_OF_ELEMENTS / ((hshtbl.maxBucketId + 1) * Bucket.bucketSize));
        System.out.println("size of highCollisedValues: " + hshtbl.highCollisedValues.size());
        System.out.println("mngr.writeTimeDelta: " + mngr.writeTimeDelta / 1000_000 + " ms, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / (mngr.writeTimeDelta / 1000) + " Mb/s");
        System.out.println("mngr.readTimeDelta: " + mngr.readTimeDelta / 1000_000 + " ms, Speed: " + ((double) (((OndiskBucketManager)hshtbl.bucketManager).counterOfWritings)) * (Bucket.bucketSize + 1) * 8 / (mngr.readTimeDelta / 1000) + " Mb/s");
        System.out.println("mngr.cachePriorityQueueTimeDelta: " + mngr.cachePriorityQueueTimeDelta / 1000_000 + " ms");

        System.out.println ("<============================================>\n");

        random.setSeed(0);

        shuffleArray(seeds, 4);

        start = System.nanoTime();

        long[] valuesForCurrentSeed = new long[NUMBER_OF_ELEMENTS_FOR_ONE_SEED];
        int indexCounter = 0;
        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
                valuesForCurrentSeed = LongStream.generate(() -> random.nextLong(MAX_VALUE_FOR_GENERATING)).limit(NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();
                shuffleArray(valuesForCurrentSeed, i);
                indexCounter = 0;
            }

            var val = valuesForCurrentSeed[indexCounter++];
            if (hshtbl.valueExists(val) == false) {
                System.out.println ("Value " + val + " inserted but not found(");
            }
        }

        end = System.nanoTime();

        System.out.println ("Time of finding of all inserted values: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");
        System.out.println ("\n<============================================>\n");

        random.setSeed(0);

        start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
                valuesForCurrentSeed = LongStream.generate(() -> random.nextLong(MAX_VALUE_FOR_GENERATING)).limit(NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();
                shuffleArray(valuesForCurrentSeed, i);
                indexCounter = 0;
            }

            var val = valuesForCurrentSeed[indexCounter++];
            hshtbl.deleteValue(val);
        }
        System.out.println();

        end = System.nanoTime();

        System.out.println ("Time of deleting of all inserted values: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");
        System.out.println ("\n<============================================>\n");

        random.setSeed(0);

        start = System.nanoTime();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++)
        {
            if (i % NUMBER_OF_ELEMENTS_FOR_ONE_SEED == 0) {
                random.setSeed(seeds[i / NUMBER_OF_ELEMENTS_FOR_ONE_SEED]);
                valuesForCurrentSeed = LongStream.generate(() -> random.nextLong(MAX_VALUE_FOR_GENERATING)).limit(NUMBER_OF_ELEMENTS_FOR_ONE_SEED).toArray();
                shuffleArray(valuesForCurrentSeed, i);
                indexCounter = 0;
            }

            var val = valuesForCurrentSeed[indexCounter++];
            if (hshtbl.valueExists(val) == true) {
                System.out.println ("Value " + val + " deleted but found(");
            }
        }
        System.out.println();

        end = System.nanoTime();

        System.out.println ("Time of checking table for emptiness: " + (end - start) / 1_000_000 + " ms, " + ((double) NUMBER_OF_ELEMENTS) / (((double) (end - start)) / 1_000_000) + " el/ms");

        hshtbl.bucketManager.clear();
    }

    // Demonstrate hash collisions
    static void demonstrateHashCollisions() {
        FractionalHasher hasher = new FractionalHasher();

        System.out.println(hasher.generateHash(11L));
        System.out.println(hasher.generateHash(100011L));
    }

    // Check hashes unique
    static void checkHashesUnique() {
        Random random = new Random();
        random.setSeed(0);

        var NUMBER_OF_ELEMENTS = 20_000_000;
        var MAX_VALUE_FOR_GENERATING = 10_000_000_000L;
        //var MAX_VALUE_FOR_GENERATING = Long.MAX_VALUE;
        //var MAX_VALUE_FOR_GENERATING = NUMBER_OF_ELEMENTS * 5L;

        //FractionalHasher hasher = new FractionalHasher();
        UniversalHasher hasher = new UniversalHasher();
        //GuavaHasher hasher = new GuavaHasher();
        HashSet<Integer> hashes = new HashSet<>();


        for (var j = 0; j < NUMBER_OF_ELEMENTS; j++) {
            var val = random.nextLong(MAX_VALUE_FOR_GENERATING);

//            System.out.println(val);
            hashes.add(hasher.generateHash(val));
        }

        System.out.println("size of hashes: " + hashes.size());
    }
    // ------------------------------------------------------

    public static void main(String[] args) throws IOException {

        // For async profiler
        /*
        long pid = ProcessHandle.current().pid();
        System.out.println("pid: ");
        System.out.println(pid);
        try {
            TimeUnit.MILLISECONDS.sleep(10_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        // Heap info
        /*long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("heapSize: " + heapSize);
        System.out.println("heapMaxSize: " + heapMaxSize);
        System.out.println("heapFreeSize: " + heapFreeSize);*/

        //generateShuffleOfRandomSequence();
        //generateBucketsWriteRead();
        //diskWriteReadOfNumbers();
        //hashtableFillingExample();
        //checkHashesUnique();
        generateHashTableFindElementsDeleteElements();
        //generateHashTableFindShuffledElementsDeleteShuffledElements();
    }
}
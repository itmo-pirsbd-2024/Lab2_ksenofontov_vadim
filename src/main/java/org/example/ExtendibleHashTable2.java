package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ExtendibleHashTable2 {
    public HashMap<Long, Long> directories;
    private int globalDepth;
    private long currentModule;

    int maxLocalDepth = 0;
    int maxBucketId = 0;
    int counterOfMovings = 0;

    HashSet<Long> highCollisedValues;

    private IHasher hasher;
    IBucketManager bucketManager;

    ExtendibleHashTable2(IBucketManager _bucketManager, IHasher _hasher) throws IOException {
        globalDepth = 1;
        currentModule = 2L;
        bucketManager = _bucketManager;
        directories = new HashMap<Long, Long>();
        hasher = _hasher;

        highCollisedValues = new HashSet<Long>();

        directories.put(0L, bucketManager.allocateNewBucket());
        directories.put(1L, bucketManager.allocateNewBucket());
    }

    boolean valueExists(long value) throws IOException {
        if (highCollisedValues.contains(value)) {
            return true;
        }

        var valueHash = hasher.generateHash(value);

        var bucketNumber = directories.get(((long)valueHash) % currentModule);

        var currentModuleCopy = currentModule;

        while (bucketNumber == null) {
            currentModuleCopy /= 2L;
            bucketNumber = directories.get(((long)valueHash) % currentModuleCopy);
        }

        var bucket = bucketManager.getBucketById(bucketNumber);

        return bucket.values.contains(value);
    };

    void deleteValue(long value) throws IOException {
        highCollisedValues.remove(value);

        var valueHash = hasher.generateHash(value);
        var bucketNumber = directories.get(((long)valueHash) % currentModule);

        var currentModuleCopy = currentModule;

        while (bucketNumber == null) {
            currentModuleCopy /= 2L;
            bucketNumber = directories.get(((long)valueHash) % currentModuleCopy);
        }

        var bucket = bucketManager.getBucketById(bucketNumber);

        bucket.values.removeIf(cur -> cur.equals(value));
    };

    boolean insertValue(long value) throws IOException {

        if (valueExists(value)) {
            return false;
        }

        var valueHash = hasher.generateHash(value);
        //System.out.println(value + " -> " + valueHash + " (hashbin: " + Integer.toBinaryString(valueHash) + ")");
        var bucketNumber = directories.get(((long)valueHash) % currentModule);

        var currentModuleCopy = currentModule;

        while (bucketNumber == null) {
            currentModuleCopy /= 2L;
            bucketNumber = directories.get(((long)valueHash) % currentModuleCopy);
        }

        var bucket = bucketManager.getBucketById(bucketNumber);

        if (bucket.values.size() < Bucket.bucketSize) {
            bucket.values.add(value);
            return true;
        }

        if ((bucket.localDepth == globalDepth) && (globalDepth == 62)) {
            highCollisedValues.add(value);
            return true;
        }

        if (bucket.localDepth == globalDepth) {
            globalDepth++;
            currentModule *= 2L;
            //System.out.println("currentModule: " + currentModule + ", globalDepth: " + globalDepth + ", valueHash: " + Long.toBinaryString(valueHash));
        }

        if (bucket.localDepth < globalDepth) {

            var newBucketId = bucketManager.allocateNewBucket();

            if (newBucketId > maxBucketId) {
                maxBucketId = (int) newBucketId;
            }

            var newBucket = bucketManager.getBucketById(newBucketId);

            // add directory pointed for new bucket
            directories.put(((1L << bucket.localDepth) + (((long)valueHash) % currentModuleCopy)), newBucketId);
            
            Iterator<Long> i = bucket.values.iterator();
            Long val;
            int valHash;
            while (i.hasNext()) {
                val = i.next();
                valHash = hasher.generateHash(val);
                if ((((long)valHash) >> bucket.localDepth) % 2L == 1) {
                    newBucket.values.add(val);
                    i.remove();

                    counterOfMovings++;
                }
            }

            //
            bucket.localDepth++;
            if (bucket.localDepth > maxLocalDepth){
                maxLocalDepth = bucket.localDepth;
            }
            //

            newBucket.localDepth = bucket.localDepth;
        }

        return insertValue(value);
    };
}

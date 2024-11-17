package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ExtendibleHashTable {
    public ArrayList<Long> directories;
    private int globalDepth;
    private long currentModule;

    int maxLocalDepth = 0;
    int maxBucketId = 0;
    int counterOfMovings = 0;

    HashSet<Long> highCollisedValues;

    private IHasher hasher;
    IBucketManager bucketManager;

    ExtendibleHashTable(IBucketManager _bucketManager, IHasher _hasher) throws IOException {
        globalDepth = 1;
        currentModule = 2;
        bucketManager = _bucketManager;
        directories = new ArrayList<Long>();
        hasher = _hasher;

        highCollisedValues = new HashSet<Long>();

        directories.add(bucketManager.allocateNewBucket());
        directories.add(bucketManager.allocateNewBucket());
    }

    boolean valueExists(long value) throws IOException {
        if (highCollisedValues.contains(value)) {
            return true;
        }

        var valueHash = hasher.generateHash(value);
        var bucketNumber = directories.get(valueHash % (int) currentModule);
        var bucket = bucketManager.getBucketById(bucketNumber);

        return bucket.values.contains(value);
    };

    void deleteValue(long value) throws IOException {
        highCollisedValues.remove(value);

        var valueHash = hasher.generateHash(value);
        var bucketNumber = directories.get(valueHash % (int)currentModule);
        var bucket = bucketManager.getBucketById(bucketNumber);

        bucket.values.removeIf(cur -> cur.equals(value));
    };

    boolean insertValue(long value) throws IOException {

        if (valueExists(value)) {
            return false;
        }

        var valueHash = hasher.generateHash(value);
        //System.out.println(value + " -> " + valueHash + " (hashbin: " + Integer.toBinaryString(valueHash) + ")");
        var bucketNumber = directories.get(valueHash % (int)currentModule);

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
            var newDirectories = new ArrayList<Long>(directories.size() * 2);

            newDirectories.addAll(directories);
            newDirectories.addAll(directories);

            directories = newDirectories;
            globalDepth++;
            currentModule *= 2;
        }

        if (bucket.localDepth < globalDepth) {

            var newBucketId = bucketManager.allocateNewBucket();

            if (newBucketId > maxBucketId) {
                maxBucketId = (int) newBucketId;
            }

            var newBucket = bucketManager.getBucketById(newBucketId);

            // change half of directories pointed for old bucket
            for (long i = (valueHash % (1L << bucket.localDepth)) + (1L << bucket.localDepth); i < directories.size(); i+= (1L << bucket.localDepth) * 2) {
                directories.set((int) i, newBucketId);
            }

            Iterator<Long> i = bucket.values.iterator();
            Long val;
            int valHash;
            while (i.hasNext()) {
                val = i.next();
                valHash = hasher.generateHash(val);
                if ((valHash >> bucket.localDepth) % 2 == 1) {
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

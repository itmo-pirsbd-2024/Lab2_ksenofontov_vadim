package org.example;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

class BucketPriorityComparator implements Comparator<Long> {
    Map<Long, Long> bucketsPriorities;

    BucketPriorityComparator(Map<Long, Long> _bucketsPriorities) {
        bucketsPriorities = _bucketsPriorities;
    }

    @Override
    public int compare(Long bucketId1, Long bucketId2) {
        int value = bucketsPriorities.get(bucketId1).compareTo(bucketsPriorities.get(bucketId2));
        //sorting elements from maximal to minimal
        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}

class BucketCache {
    public int maxBucketsInCache;

    PriorityQueue<Long> bucketIdsPriorityQueue;
    Map<Long, Bucket> cachedBuckets;
    Map<Long, Long> bucketsPriorities;
    long maxCurrentBucketPriority;

    OndiskBucketManager ondiskBucketManager;

    BucketCache(OndiskBucketManager _ondiskBucketManager, int _maxBucketsInCache) {
        ondiskBucketManager = _ondiskBucketManager;
        maxBucketsInCache = _maxBucketsInCache;
        bucketsPriorities = new HashMap<>();
        bucketIdsPriorityQueue = new PriorityQueue<>(new BucketPriorityComparator(bucketsPriorities));
        cachedBuckets = new HashMap<>();
        maxCurrentBucketPriority = 0;
    }

    public Bucket getBucketById(long bucketId) throws IOException {

        if (cachedBuckets.containsKey(bucketId) == false) {
            var bucketFromDisk = ondiskBucketManager.readBucketFromDisk(bucketId);

            bucketsPriorities.put(bucketId, ++maxCurrentBucketPriority);

            addBucketInCache(bucketId, bucketFromDisk);
        }
        else {
            var start = System.nanoTime();
            bucketIdsPriorityQueue.remove(bucketId);
            var end = System.nanoTime();
            ondiskBucketManager.cachePriorityQueueTimeDelta += (end - start);

            bucketsPriorities.put(bucketId, ++maxCurrentBucketPriority);

            start = System.nanoTime();
            bucketIdsPriorityQueue.add(bucketId);
            end = System.nanoTime();
            ondiskBucketManager.cachePriorityQueueTimeDelta += (end - start);
        }

        return cachedBuckets.get(bucketId);
    }

    public void addBucketInCache(long bucketId, Bucket bucket) throws IOException {
        if (bucketIdsPriorityQueue.size() == maxBucketsInCache) {

            var start = System.nanoTime();
            var bucketIdToDelete = bucketIdsPriorityQueue.poll();
            var end = System.nanoTime();
            ondiskBucketManager.cachePriorityQueueTimeDelta += (end - start);

            var bucketForWriteToDisk = cachedBuckets.get(bucketIdToDelete);
            cachedBuckets.remove(bucketIdToDelete);
            bucketsPriorities.remove(bucketIdToDelete);
            ondiskBucketManager.writeBucketToDisk(bucketIdToDelete, bucketForWriteToDisk);
        }

        cachedBuckets.put(bucketId, bucket);

        var start = System.nanoTime();
        bucketIdsPriorityQueue.add(bucketId);
        var end = System.nanoTime();
        ondiskBucketManager.cachePriorityQueueTimeDelta += (end - start);
    }
}

public class OndiskBucketManager implements IBucketManager {

    RandomAccessFile fileObject;
    long maxAllocatedBucketId = -1;
    BucketCache cache;

    public long counterOfWritings = 0;
    public long counterOfReadings = 0;

    public long writeTimeDelta = 0;
    public long readTimeDelta = 0;

    public long cachePriorityQueueTimeDelta = 0;

    OndiskBucketManager(String _pathToFile) throws IOException {
        fileObject = new RandomAccessFile(_pathToFile, "rw");
        cache = new BucketCache(this, 2);
    }

    byte[] serializeBucket(Bucket bucket) {
        byte[] bytes = new byte[(Bucket.bucketSize + 1) * 8];
        var buffer = ByteBuffer.wrap(bytes).asLongBuffer();

        Iterator<Long> i = bucket.values.iterator();
        Long val;
        long currentSizeAndLocalDepthLong = 0;
        currentSizeAndLocalDepthLong |= bucket.values.size();
        currentSizeAndLocalDepthLong |= ((long) bucket.localDepth) << 32;

        buffer.put(currentSizeAndLocalDepthLong);

        while (i.hasNext()) {
            val = (Long)i.next();
            buffer.put(val);
        }

        return bytes;
    }

    Bucket deserializeBucket(byte[] bytes) {

        Bucket deserializedBucket = new Bucket();
        var buffer = ByteBuffer.wrap(bytes).asLongBuffer();

        long currentSizeAndLocalDepthLong = buffer.get();
        long val;

        var numberOfValues = (int) currentSizeAndLocalDepthLong;
        deserializedBucket.localDepth = (int) (currentSizeAndLocalDepthLong >> 32);

        for (int j = 0; j < numberOfValues; j++) {
            val = buffer.get();
            deserializedBucket.values.add(val);
        }

        return deserializedBucket;
    }

    Bucket readBucketFromDisk(long bucketId) throws IOException {
        counterOfReadings++;

        var bucketBytes = new byte[(Bucket.bucketSize + 1) * 8];

        var start = System.nanoTime();
        fileObject.seek(((Bucket.bucketSize + 1) * 8L) * bucketId);
        fileObject.read(bucketBytes, 0, (Bucket.bucketSize + 1) * 8);
        var end = System.nanoTime();
        readTimeDelta += (end - start);

        return deserializeBucket(bucketBytes);
    }

    void writeBucketToDisk(long bucketId, Bucket bucket) throws IOException {
        counterOfWritings++;

        var bucketBytes = serializeBucket(bucket);

        var start = System.nanoTime();
        fileObject.seek(((Bucket.bucketSize + 1) * 8L) * bucketId);
        fileObject.write(bucketBytes);
        var end = System.nanoTime();
        writeTimeDelta += (end - start);
    }

    @Override
    public long allocateNewBucket() throws IOException {
        var newBucket = new Bucket();
        var newBucketId = maxAllocatedBucketId + 1;
        maxAllocatedBucketId++;

        cache.bucketsPriorities.put(newBucketId, ++cache.maxCurrentBucketPriority);

        cache.addBucketInCache(newBucketId, newBucket);
        return newBucketId;
    }

    @Override
    public Bucket getBucketById(long bucketId) throws IOException {
        return cache.getBucketById(bucketId);
    }

    @Override
    public void clear() throws IOException {
        fileObject.setLength(0);
        fileObject.close();
    }
}

package org.example;

import java.util.ArrayList;

public class InmemoryBucketManager implements IBucketManager {
    ArrayList<Bucket> buckets = new ArrayList<Bucket>();

    @Override
    public long allocateNewBucket(){
        buckets.add(new Bucket());
        return buckets.size() - 1;
    }

    @Override
    public Bucket getBucketById(long bucketId) {
        return buckets.get((int) bucketId);
    }

    @Override
    public void clear() {}
}

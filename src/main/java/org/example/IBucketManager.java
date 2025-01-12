package org.example;

import java.io.IOException;

public interface IBucketManager {
    long allocateNewBucket() throws IOException;
    Bucket getBucketById(long bucketId) throws IOException;
    void clear() throws IOException;
}


package org.example;

public class UniversalHasher implements IHasher {
    long a = 598653L;
    long b = 3745213L;
    long p = 10000000319L;
    int maxHash = Integer.MAX_VALUE;

    public int generateHash(long value) {
        return (int) (((a * value + b) % p) % maxHash);
    }

}
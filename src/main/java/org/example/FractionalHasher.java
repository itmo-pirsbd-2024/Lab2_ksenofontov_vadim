package org.example;

public class FractionalHasher implements IHasher {
    double A = 0.66667;
    int maxHash = Integer.MAX_VALUE;

    public int generateHash(long value) {
        return ((int) Math.floor((value * A - Math.floor(value * A)) * maxHash));
    }

}

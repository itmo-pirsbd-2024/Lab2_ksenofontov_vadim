package org.example;

public class GuavaHasher implements IHasher  {

    public int generateHash(long value) {

        value ^= (value >>> 20) ^ (value >>> 12);
        var toInt = (int) (value ^ (value >>> 7) ^ (value >>> 4));
        if (toInt < 0) {
            return -toInt;
        }
        return toInt;
    }

}

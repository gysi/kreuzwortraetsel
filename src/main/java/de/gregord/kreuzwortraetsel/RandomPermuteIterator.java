package de.gregord.kreuzwortraetsel;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.util.Enumeration;
import java.util.Random;
import java.util.SplittableRandom;

public class RandomPermuteIterator implements Enumeration<Long> {
    int c = 1013904223, a = 1664525;
    long seed, N, m, next;
    boolean hasNext = true;
    static XoRoShiRo128PlusRandom fastRandom = new XoRoShiRo128PlusRandom();
    static SplittableRandom random = new SplittableRandom();
    public RandomPermuteIterator(long N) {
        // optimized for speed
//        if (N <= 0 || N > Math.pow(2, 62)) throw new RuntimeException("Unsupported size: " + N);
        this.N = N;
        m = (long) Math.pow(2, Math.ceil(Math.log(N) / Math.log(2)));
        next = seed = random.nextInt((int) Math.min(N, Integer.MAX_VALUE));
//        next = seed = fastRandom.nextInt((int) N); // optimized for speed

    }
//
    public static void main(String[] args) throws Exception {
        RandomPermuteIterator r = new RandomPermuteIterator(3);
        while (r.hasMoreElements()) System.out.print(r.nextElement() + " ");
        //output:50 52 3 6 45 40 26 49 92 11 80 2 4 19 86 61 65 44 27 62 5 32 82 9 84 35 38 77 72 7 ...
    }

    @Override
    public boolean hasMoreElements() {
        return hasNext;
    }

    @Override
    public Long nextElement() {
        next = (a * next + c) % m;
        while (next >= N) next = (a * next + c) % m;
        if (next == seed) hasNext = false;
        return  next;
    }
}

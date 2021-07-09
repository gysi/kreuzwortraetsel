package de.gregord.kreuzwortraetsel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Statistics {

    public static class Bucket {
        private final int wordCount;
        private final int maxEmptyFields;
        private final AtomicInteger puzzleSolvedCount = new AtomicInteger(0);
        private final List<AtomicInteger> emptyFieldsCount = new ArrayList<>();

        public Bucket(int wordCount, int maxEmptyFields) {
            this.wordCount = wordCount;
            this.maxEmptyFields = maxEmptyFields;
            for (int i = 0; i < maxEmptyFields; i++) {
                emptyFieldsCount.add(new AtomicInteger(0));
            }
        }

        public void addToBucket(SolvedPuzzleInfo solvedPuzzleInfo) {
            puzzleSolvedCount.incrementAndGet();
            emptyFieldsCount.get(solvedPuzzleInfo.getField().getEmptyFieldCount()).incrementAndGet();
        }

        public AtomicInteger getPuzzleSolvedCount() {
            return puzzleSolvedCount;
        }

        public List<AtomicInteger> getEmptyFieldsCount() {
            return emptyFieldsCount;
        }
    }

    private final List<String> wordList;
    private final List<String> optionalWordList;
    private final List<Bucket> buckets = new ArrayList<>();

    public Statistics(List<String> wordList, List<String> optionalWordList, Settings settings) {
        this.wordList = wordList;
        this.optionalWordList = optionalWordList;
        int maxEmptyFields = calculateMaxEmptyFields(settings);
        for (int i = 0; i < wordList.size() + optionalWordList.size(); i++) {
            buckets.add(new Bucket(i, maxEmptyFields));
        }
    }

    private int calculateMaxEmptyFields(Settings settings) {
        return (settings.getFieldWidth() * settings.getFieldHeight())
                - (settings.getBlockedArea().getWidth() * settings.getBlockedArea().getHeight());
    }

    public void addToStatistics(SolvedPuzzleInfo solvedPuzzleInfo) {
        buckets.get(solvedPuzzleInfo.getIterations()).addToBucket(solvedPuzzleInfo);
        if (solvedPuzzleInfo.getIterations() == 2) {
            //            System.out.println("wtf?");
        }
    }

    public void print() {
        for (int i = 0; i < buckets.size(); i++) {
            Bucket bucket = buckets.get(i);
            System.out.println("Puzzles with " + (i + 1) + " words: " + bucket.getPuzzleSolvedCount().get());
            List<AtomicInteger> emptyFieldsCount = bucket.getEmptyFieldsCount();
            if (bucket.getPuzzleSolvedCount().get() > 0) {
                System.out.println("EmptyFields count from this puzzles");
                for (int j = 0; j < emptyFieldsCount.size(); j++) {
                    AtomicInteger puzzlesWithThisEmptyFieldCount = emptyFieldsCount.get(j);
                    if (puzzlesWithThisEmptyFieldCount.get() > 0) {
                        System.out.println("Puzzles with " + j + " empty fields left: " + puzzlesWithThisEmptyFieldCount.get());
                    }
                }
            }
            System.out.println();
        }
    }
}

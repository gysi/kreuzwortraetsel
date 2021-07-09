package de.gregord.kreuzwortraetsel;

import java.util.List;

public class SolvedPuzzleInfo {
    private final List<PlacedWordInfo> placedWordInfoList;
    private final int iterations;
    private final Field field;
    private final List<String> missingWordsList;
    private final List<String> missingOptionalWordsList;
    private final List<String> firstWordToBeUsed;
    private final List<String> lastWordToBeUsed;

    public SolvedPuzzleInfo(Field field, int iterations,
            List<String> missingWordsList, List<String> missingOptionalWordsList,
            List<PlacedWordInfo> placedWordInfoList, List<String> firstWordsToBeUsed, List<String> lastWordsToBeUsed) {
        this.field = field;
        this.iterations = iterations;
        this.placedWordInfoList = placedWordInfoList;
        this.missingWordsList = missingWordsList;
        this.missingOptionalWordsList = missingOptionalWordsList;
        this.firstWordToBeUsed = firstWordsToBeUsed;
        this.lastWordToBeUsed = lastWordsToBeUsed;
    }

    public List<PlacedWordInfo> getPlacedWordInfoList() {
        return placedWordInfoList;
    }

    public int getIterations() {
        return iterations;
    }

    public Field getField() {
        return field;
    }

    public List<String> getMissingWordsList() {
        return missingWordsList;
    }

    public List<String> getMissingOptionalWordsList() {
        return missingOptionalWordsList;
    }

    public List<String> getFirstWordToBeUsed() {
        return firstWordToBeUsed;
    }

    public List<String> getLastWordToBeUsed() {
        return lastWordToBeUsed;
    }
}

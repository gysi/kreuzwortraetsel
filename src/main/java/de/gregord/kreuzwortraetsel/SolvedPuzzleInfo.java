package de.gregord.kreuzwortraetsel;

import java.util.List;

public class SolvedPuzzleInfo {
    private final int iterations;
    private final Field field;
    private final List<String> missingWordsList;
    private final List<String> missingOptionalWordsList;
    private final List<PlacedWordInfo> placedWordInfoList;

    public SolvedPuzzleInfo(Field field, int iterations,
            List<String> missingWordsList, List<String> missingOptionalWordsList,
            List<PlacedWordInfo> placedWordInfoList) {
        this.field = field;
        this.iterations = iterations;
        this.placedWordInfoList = placedWordInfoList;
        this.missingWordsList = missingWordsList;
        this.missingOptionalWordsList = missingOptionalWordsList;
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
}

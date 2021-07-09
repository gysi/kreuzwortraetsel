package de.gregord.kreuzwortraetsel;

import java.util.List;

public class Settings {
    private int iterations;
    private int threadCount;
    private List<String> wordList;
    private List<String> optionalWordList;
    private BlockedArea blockedArea;
    private int fieldWidth;
    private int fieldHeight;

    public Settings(){}

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }

    public BlockedArea getBlockedArea() {
        return blockedArea;
    }

    public void setBlockedArea(BlockedArea blockedArea) {
        this.blockedArea = blockedArea;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
        this.fieldHeight = fieldHeight;
    }

    public List<String> getOptionalWordList() {
        return optionalWordList;
    }

    public void setOptionalWordList(List<String> optionalWordList) {
        this.optionalWordList = optionalWordList;
    }
}

package de.gregord.kreuzwortraetsel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver {
    public static final Logger LOG = LoggerFactory.getLogger(Solver.class);

    public static class Shuffler {
        private final List<String> firstWordsToBeUsed;
        private final List<String> lastWordsToBeUsed;

        public Shuffler(List<String> wordList){
            int wordCount = wordList.size();
            int lastWordsToBeUsedPercentage = (int)(wordCount * 0.3);
            wordList.sort(Comparator.comparingInt(String::length));
            this.lastWordsToBeUsed = wordList.subList(0, lastWordsToBeUsedPercentage);
            this.firstWordsToBeUsed = wordList.subList(lastWordsToBeUsedPercentage, wordList.size());
        }

        public List<String> shuffle(){
            Collections.shuffle(this.firstWordsToBeUsed);
            Collections.shuffle(this.lastWordsToBeUsed);
            List<String> shuffledList = new ArrayList<>();
            shuffledList.addAll(this.lastWordsToBeUsed);
            shuffledList.addAll(this.firstWordsToBeUsed);
            return shuffledList;
        }

        public List<String> getFirstWordsToBeUsed() {
            return firstWordsToBeUsed;
        }

        public List<String> getLastWordsToBeUsed() {
            return lastWordsToBeUsed;
        }
    }

    private Field field;
    private List<PlacedWordInfo> wordsPlaced = new ArrayList<>();
    private final WordPlacer wordPlacer;
    private int maxIteration = 0;
    private final int width;
    private final int height;
    private final BlockedArea blockedArea;
    private final Shuffler shuffler;
    private final List<String> optionalWordList;

    public Solver(int width, int height, List<String> words, List<String> optionalWordList, BlockedArea blockedArea) {
        this.wordPlacer = new WordPlacer(new Field(width, height, blockedArea));
        this.width = width;
        this.height = height;
        this.blockedArea = blockedArea;
        this.optionalWordList = optionalWordList;
        shuffler = new Shuffler(new ArrayList<>(words));
    }

    public SolvedPuzzleInfo solve(int missingWordsLimit) {
        LOG.debug("starting recursive solve");
        wordsPlaced = new ArrayList<>();
        field = new Field(width, height, blockedArea);
        maxIteration = 0;
        List<String> wordsLeftInLastSolve = shuffler.shuffle();
        recursiveSolve(wordsLeftInLastSolve, 0);
        List<String> optionalWordsLeftInLastSolve = new ArrayList<>(this.optionalWordList);
        if(wordsLeftInLastSolve.size() <= missingWordsLimit){
            recursiveSolve(optionalWordsLeftInLastSolve, maxIteration + 1);
        }
        return new SolvedPuzzleInfo(field, maxIteration, wordsLeftInLastSolve, optionalWordsLeftInLastSolve, wordsPlaced,
                shuffler.getFirstWordsToBeUsed(), shuffler.getLastWordsToBeUsed());
    }

    private void recursiveSolve(List<String> words, int iteration) {
        LOG.debug("Iteration: " + iteration);
        if(iteration == 0){
            boolean tryanotherLocation = true;
            while(tryanotherLocation) {
                tryanotherLocation = false;
                String removedWord = words.remove(words.size() - 1);
                PlacedWordInfo placedWordInfo = wordPlacer.placeInitialWord(removedWord, field);
                wordsPlaced.add(placedWordInfo);
                recursiveSolve(words, iteration + 1);
                if(maxIteration == iteration){
                    LOG.debug("first recursive solve couldn't place word after: " + removedWord);
                    tryanotherLocation = true;
                    words.add(removedWord);
                    wordsPlaced.remove(placedWordInfo);
                    for (Letter letter : placedWordInfo.letters()) {
                        letter.resetToLastState();
                    }
                }
            }
            return;
        }
        WordPlacer.PlaceWordResult placeWordResult = wordPlacer.placeWordFromList(words, field);
        if(placeWordResult.couldBePlaced()){
            LOG.debug("A word was placed ("+placeWordResult.word()+"), remaining:");
            if(LOG.isTraceEnabled()){
                for (String word : words) {
                    LOG.trace(word);
                }
            }
            wordsPlaced.add(placeWordResult.placedWordInfo());
            if(maxIteration < iteration){
                maxIteration = iteration;
            }
            if(words.size() > 0){
                recursiveSolve(words, iteration + 1);
                return;
            }
        }
        if(!placeWordResult.couldBePlaced()) {
            LOG.trace("No word could be placed. iteration: " + iteration);
        }
        return;
    }
}

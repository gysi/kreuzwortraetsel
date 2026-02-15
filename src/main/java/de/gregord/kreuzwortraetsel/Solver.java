package de.gregord.kreuzwortraetsel;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver {
    public static final Logger LOG = LoggerFactory.getLogger(Solver.class);

    public static class Shuffler {
        private final List<String> wordList;

        public Shuffler(List<String> wordList){
            this.wordList = wordList;
        }

        public List<String> allRandom(){
            ArrayList<String> copy = new ArrayList<>(wordList);
            Collections.shuffle(copy);
            return copy;
        }

        public List<String> fastShuffle(){
            RandomPermuteIterator r = new RandomPermuteIterator(wordList.size());
            ArrayList<String> shuffledList = new ArrayList<>(wordList.size());
            while (r.hasMoreElements()){
                shuffledList.add(wordList.get(Math.toIntExact(r.nextElement())));
            }
            return shuffledList;
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
    private HashMap<Character, List<Position>> letterPositionMap = new HashMap<>();

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
        letterPositionMap = new HashMap<>(50);
        if(field == null){
            field = new Field(width, height, blockedArea);
        }else {
            field.resetField();
        }
        maxIteration = 0;
        List<String> wordsLeftInLastSolve = shuffler.fastShuffle();
        iterativeSolve(wordsLeftInLastSolve, 0);
        List<String> optionalWordsLeftInLastSolve = new ArrayList<>(this.optionalWordList);
        if(wordsLeftInLastSolve.size() <= missingWordsLimit){
            iterativeSolve(optionalWordsLeftInLastSolve, maxIteration + 1);
        }
        return new SolvedPuzzleInfo(field, maxIteration, wordsLeftInLastSolve, optionalWordsLeftInLastSolve, wordsPlaced);
    }

    private void iterativeSolve(List<String> words, int iteration){
        while(words.size() > 0) {
            if (iteration == 0) {
                if (wordsPlaced.size() == 1) {
                    PlacedWordInfo remove = wordsPlaced.remove(0);
                    words.add(remove.word());
                    removeFromLetterPositionMap(remove);
                    LOG.debug("first solve couldn't place word after: " + remove.word());
                    for (Letter letter : remove.letters()) {
                        letter.resetToLastState();
                    }
                }
                String removedWord = words.remove(words.size() - 1);
                PlacedWordInfo placedWordInfo = wordPlacer.placeInitialWord(removedWord, field);
                wordsPlaced.add(placedWordInfo);
                addToLetterPositionMap(placedWordInfo);
            }
//            PlacedWordInfo placedWordInfo = wordPlacer.placeWordFromList(words, field);
            PlacedWordInfo placedWordInfo = wordPlacer.placeWordFromListV3(words, field, letterPositionMap);
            if(placedWordInfo != null){
                iteration++;
                addToLetterPositionMap(placedWordInfo);
                LOG.debug("A word was placed ("+placedWordInfo.word()+"), remaining:");
                if(LOG.isTraceEnabled()){
                    for (String word : words) {
                        LOG.trace(word);
                    }
                }
                wordsPlaced.add(placedWordInfo);
                if(maxIteration < iteration){
                    maxIteration = iteration;
                }
            }
            if(placedWordInfo == null) {
                LOG.trace("No word could be placed. iteration: " + iteration);
                if(iteration > 0){
                    return;
                }
            }
        }
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
                addToLetterPositionMap(placedWordInfo);
                recursiveSolve(words, iteration + 1);
                if(maxIteration == iteration){
                    LOG.debug("first recursive solve couldn't place word after: " + removedWord);
                    tryanotherLocation = true;
                    words.add(removedWord);
                    wordsPlaced.remove(placedWordInfo);
                    removeFromLetterPositionMap(placedWordInfo);
                    for (Letter letter : placedWordInfo.letters()) {
                        letter.resetToLastState();
                    }
                }
            }
            return;
        }
//        PlacedWordInfo placedWordInfo = wordPlacer.placeWordFromList(words, field);
        PlacedWordInfo placedWordInfo = wordPlacer.placeWordFromListV2(words, field, letterPositionMap);
        if(placedWordInfo != null){
            addToLetterPositionMap(placedWordInfo);
            LOG.debug("A word was placed ("+placedWordInfo.word()+"), remaining:");
            if(LOG.isTraceEnabled()){
                for (String word : words) {
                    LOG.trace(word);
                }
            }
            wordsPlaced.add(placedWordInfo);
            if(maxIteration < iteration){
                maxIteration = iteration;
            }
            if(words.size() > 0){
                recursiveSolve(words, iteration + 1);
                return;
            }
        }
        if(placedWordInfo == null) {
            LOG.trace("No word could be placed. iteration: " + iteration);
        }
        return;
    }

    private void addToLetterPositionMap(PlacedWordInfo placedWordInfo){
        for (Letter letter : placedWordInfo.letters()) {
            letterPositionMap.computeIfAbsent(letter.getChar(), character -> new ArrayList<>(100)).add(new Position(letter.posX, letter.posY));
        }
    }

    private void removeFromLetterPositionMap(PlacedWordInfo placedWordInfo){
        for (Letter letter : placedWordInfo.letters()) {
            List<Position> positions = letterPositionMap.get(letter.getChar());
            positions.remove(positions.size()-1);
        }
    }
}

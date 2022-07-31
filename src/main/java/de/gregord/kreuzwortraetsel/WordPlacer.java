package de.gregord.kreuzwortraetsel;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordPlacer {
    public static final Logger LOG = LoggerFactory.getLogger(WordPlacer.class);
    private final Map<Integer, List<StartingPoint>> startingPointMap;
    private final Random random = new Random();

    private static record StartingPoint(int posx, int posy, Orientation orientation) { }

    public WordPlacer(Field field) {
        startingPointMap = calculateStartingPoints(field);
    }

    private Map<Integer, List<StartingPoint>> calculateStartingPoints(Field f){
        Map<Integer, List<StartingPoint>> startingPointMap = new HashMap<>();
        Letter[][] field = f.getField();
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                // go right
                Letter startLetter = field[row][col];
                if(startLetter == Letter.NULL){
                    continue;
                }
                Letter tempLetter = startLetter.rightLetter;
                int length = 1;
                while(tempLetter != Letter.NULL){
                    tempLetter = tempLetter.rightLetter;
                    length++;
                }
                for (int i = length; i >= 0; i--) {
                    startingPointMap.computeIfAbsent(i, integer -> new ArrayList<>())
                            .add(new StartingPoint(col, row, Orientation.HORIZONTAL));
                }
                // go down
                tempLetter = startLetter.bottomLetter;
                length = 1;
                while(tempLetter != Letter.NULL){
                    tempLetter = tempLetter.bottomLetter;
                    length++;
                }
                for (int i = length; i >= 0; i--) {
                    startingPointMap.computeIfAbsent(i, integer -> new ArrayList<>())
                            .add(new StartingPoint(col, row, Orientation.VERTICAL));
                }
            }
        }
        return startingPointMap;
    }

    public PlacedWordInfo placeInitialWord(String word, Field field) {
        if (field.isFieldEmpty()) {
            int wordLength = word.length();
            List<StartingPoint> startingPoints = startingPointMap.get(wordLength);
            if(startingPoints == null){
                throw new RuntimeException("No space found for initial word");
            }
            StartingPoint startingPoint = startingPoints.get(random.nextInt(startingPoints.size()));
            List<Letter> letters = field.placeWord(word, startingPoint.posx, startingPoint.posy, startingPoint.orientation);
            return new PlacedWordInfo(word, startingPoint.posx, startingPoint.posy, '_', "none", letters);
        } else {
            throw new RuntimeException("Field is not empty");
        }
    }

    public PlacedWordInfo placeWordFromList(List<String> words, Field field) {
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                for (int row = 0; row < field.height; row++) {
                    for (int col = 0; col < field.width; col++) {
                        Letter letter = field.getLetter(row, col);
                        if (letter.getChar() == word.charAt(i)) { // Matching char found
                            if (letter.orientation == Orientation.BOTH) {
                                continue;
                            }
                            if (letter.isVertical()
                                    && doesWordFit(letter, word, i, Orientation.HORIZONTAL)) {
                                LOG.trace("placeword " + (col - i) + " " + row + " " + Orientation.HORIZONTAL);
                                List<Letter> letters = field.placeWord(word, col - i, row, Orientation.HORIZONTAL);
                                words.remove(word);
                                return new PlacedWordInfo(word, col, row, letter.getChar(), letter.word, letters);
                            }
                            if (letter.isHorizontal()
                                    && doesWordFit(letter, word, i, Orientation.VERTICAL)) {
                                LOG.trace("placeword " + col + " " + (row - i) + " " + Orientation.HORIZONTAL);
                                List<Letter> letters = field.placeWord(word, col, row - i, Orientation.VERTICAL);
                                words.remove(word);
                                return new PlacedWordInfo(word, col, row, letter.getChar(), letter.word, letters);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public PlacedWordInfo placeWordFromListV2(List<String> words, Field field, HashMap<Character, List<Position>> letterPositionMap) {
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                List<Position> positions = letterPositionMap.get(word.charAt(i));
                if(positions == null){
                    continue;
                }
                for (Position position : positions) {
                    final int row = position.y();
                    final int col = position.x();
                    Letter letter = field.getLetter(row, col);
                    if (letter.getChar() == word.charAt(i)) { // Matching char found
                        if (letter.orientation == Orientation.BOTH) {
                            continue;
                        }
                        if (letter.isVertical()
                                && doesWordFit(letter, word, i, Orientation.HORIZONTAL)) {
                            LOG.trace("placeword " + (col - i) + " " + row + " " + Orientation.HORIZONTAL);
                            List<Letter> letters = field.placeWord(word, col - i, row, Orientation.HORIZONTAL);
                            words.remove(word);
                            return new PlacedWordInfo(word, col, row, letter.getChar(), letter.word, letters);
                        }
                        if (letter.isHorizontal()
                                && doesWordFit(letter, word, i, Orientation.VERTICAL)) {
                            LOG.trace("placeword " + col + " " + (row - i) + " " + Orientation.HORIZONTAL);
                            List<Letter> letters = field.placeWord(word, col, row - i, Orientation.VERTICAL);
                            words.remove(word);
                            return new PlacedWordInfo(word, col, row, letter.getChar(), letter.word, letters);
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean doesWordFit(Letter letter, String word, int wordPos, Orientation orientation) {
        if (orientation.equals(Orientation.HORIZONTAL)) {
            // look left side
            int tempPos;
            Letter tempLetter;
            if (wordPos != 0) {
                tempPos = wordPos;
                tempLetter = letter;
                while (tempPos > 0) {
                    tempPos--;
                    tempLetter = tempLetter.leftLetter;
                    if(!isHorizontalConditionMet(word.charAt(tempPos), tempLetter)){
                        return false;
                    }
                }
                if(!hasPlaceForQuestionBlock(tempLetter, Orientation.HORIZONTAL)){
                    return false;
                }
                // we need to check the left side when loop is finished
                if (tempLetter.leftLetter.isNotEmpty()) {
                    return false;
                }
            }else{
                if(!hasPlaceForQuestionBlock(letter, Orientation.HORIZONTAL)){
                    return false;
                }
                // check if left side is an entry, then its not valid
                if(letter.leftLetter.isNotEmpty()){
                    return false;
                }
            }
            // look right side
            if (wordPos != word.length() - 1) {
                tempPos = wordPos;
                tempLetter = letter;
                while (tempPos < word.length() - 1) {
                    tempPos++;
                    tempLetter = tempLetter.rightLetter;
                    if(!isHorizontalConditionMet(word.charAt(tempPos), tempLetter)){
                        return false;
                    }
                }
                // we need to check the right side when loop is finished
                if (tempLetter.rightLetter.isNotEmpty()) {
                    return false;
                }
            } else {
                // check if right side is an entry, then its not valid
                if(letter.rightLetter.isNotEmpty()){
                    return false;
                }
            }
        } else { // VERTICAL ORIENTATION
            // look top side
            int tempPos;
            Letter tempLetter;
            if (wordPos != 0) {
                tempPos = wordPos;
                tempLetter = letter;
                while (tempPos > 0) {
                    tempPos--;
                    tempLetter = tempLetter.topLetter;
                    if(!isVerticalConditionMet(word.charAt(tempPos), tempLetter)){
                        return false;
                    }
                }
                if(!hasPlaceForQuestionBlock(tempLetter, Orientation.VERTICAL)){
                    return false;
                }
                // we need to check the right side when loop is finished
                if (tempLetter.topLetter.isNotEmpty()) {
                    return false;
                }
            } else {
                if(!hasPlaceForQuestionBlock(letter, Orientation.VERTICAL)){
                    return false;
                }
                // check if top side is an entry, then its not valid
                if(letter.topLetter.isNotEmpty()){
                    return false;
                }
            }
            // look bottom side
            if (wordPos != word.length() - 1) {
                tempPos = wordPos;
                tempLetter = letter;
                while (tempPos < word.length() - 1) {
                    tempPos++;
                    tempLetter = tempLetter.bottomLetter;
                    if(!isVerticalConditionMet(word.charAt(tempPos), tempLetter)){
                        return false;
                    }
                }
                // we need to check the right side when loop is finished
                if (tempLetter.bottomLetter.isNotEmpty()) {
                    return false;
                }
            } else {
                // check if top side is an entry, then its not valid
                if(letter.bottomLetter.isNotEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasPlaceForQuestionBlock(final Letter target, final Orientation orientation){
        if(orientation == Orientation.HORIZONTAL){
            if(target.topLetter != Letter.NULL && target.topLetter.isEmpty){
                return true;
            }
            if(target.bottomLetter != Letter.NULL && target.bottomLetter.isEmpty){
                return true;
            }
            if(target.leftLetter != Letter.NULL && target.leftLetter.isEmpty){
                return true;
            }
        }else { // Vertical
            if(target.topLetter != Letter.NULL && target.topLetter.isEmpty){
                return true;
            }
            if(target.rightLetter != Letter.NULL && target.rightLetter.isEmpty){
                return true;
            }
            if(target.leftLetter != Letter.NULL && target.leftLetter.isEmpty){
                return true;
            }
        }
        return false;
    }

    private boolean isHorizontalConditionMet(char chartToCheck, Letter target){
        if (target == Letter.NULL // we have reached the end of the field
                // target letter doesn't match char or has a not valid orientation
                || (target.isNotEmpty()
                    && (chartToCheck != target.getChar()
                        || !target.isHorizontal()))
                // when target is empty on the bottom or top can't be a letter in the vertical direction
                || (target.isEmpty
                    && ((target.topLetter.isNotEmpty() && target.topLetter.isVertical())
                        || (target.bottomLetter.isNotEmpty() && target.bottomLetter.isVertical())
        ))) {
            return false;
        }
        return true;
    }

    private boolean isVerticalConditionMet(char chartToCheck, Letter target){
        if (target == Letter.NULL // we have reached the end of the field
                // target letter doesn't match char or has a not valid orientation
                || (target.isNotEmpty()
                    && (chartToCheck != target.getChar()
                        || !target.isVertical()))
                // when target is empty on the left or right can't be a letter in the horizontal
                || (target.isEmpty
                    && ((target.rightLetter.isNotEmpty() && target.rightLetter.isHorizontal())
                        || (target.leftLetter.isNotEmpty() && target.leftLetter.isHorizontal())
        ))) {
            return false;
        }
        return true;
    }
}

package de.gregord.kreuzwortraetsel;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

public class Letter {
    public boolean isFirst;
    public boolean isEmpty;
    public boolean isQuestionBlock;
    public Letter firstQuestionBlockOfThisLetter;
    public Letter secondQuestionBlockOfThisLetter;
    public String word;
    public String word2;
    public char letter;
    public Letter topLetter;
    public Letter rightLetter;
    public Letter bottomLetter;
    public Letter leftLetter;
    public int posX;
    public int posY;
    public static Letter NULL = new Letter().addTop(null).addRight(null).addBottom(null).addLeft(null);
    public Orientation occupiedOrientation = Orientation.NONE;
    public Orientation initialOrientation;
    private static final SplittableRandom random = new SplittableRandom();
//    private static final XoRoShiRo128PlusRandom random = new XoRoShiRo128PlusRandom(); // faster

    public Letter() {
        this.isEmpty = true;
    }

    public Letter addTop(Letter top){
        this.topLetter = top;
        return this;
    }

    public Letter addRight(Letter right){
        this.rightLetter = right;
        return this;
    }

    public Letter addBottom(Letter bottom){
        this.bottomLetter = bottom;
                return this;
    }

    public Letter addLeft(Letter left){
        this.leftLetter = left;
        return this;
    }

    public void reset(){
        if(this == NULL){
            return;
        }
        if(isEmpty){
            return;
        }
        this.occupiedOrientation = Orientation.NONE;
        this.word = null;
        this.letter = '\u0000'; // null char
        this.isFirst = false;
        this.isEmpty = true;
        this.initialOrientation = null;
        this.isQuestionBlock = false;
        firstQuestionBlockOfThisLetter = null;
    }

    public void resetToLastState(){
        if(this == NULL){
            return;
        }
        if(isEmpty){
            return;
        }
        if(occupiedOrientation == Orientation.BOTH){
            // two words are using this letter!
            occupiedOrientation = initialOrientation;
            word2 = null;
            if(secondQuestionBlockOfThisLetter != null){
                secondQuestionBlockOfThisLetter = null;
                if(firstQuestionBlockOfThisLetter == null){
                    isFirst = false;
                }
            }
        }else{
            this.occupiedOrientation = Orientation.NONE;
            this.word = null;
            this.letter = '\u0000'; // null char
            this.isFirst = false;
            this.isEmpty = true;
            this.initialOrientation = null;
            this.isQuestionBlock = false;
            if(firstQuestionBlockOfThisLetter != null){
                firstQuestionBlockOfThisLetter.resetToLastState();
                firstQuestionBlockOfThisLetter = null;
            }
        }
    }

    public Letter set(boolean isFirst, char letter, String word, Orientation orientation) {
        this.occupiedOrientation = orientation;
        this.word = word;
        this.letter = letter;
        this.isFirst = isFirst;
        if(this.isEmpty){
            if(isFirst) {
                setFirstQuestionBlockOfThisLetter();
            }
            this.isEmpty = false;
            this.initialOrientation = orientation;
        }else {
            if(isFirst) {
                setSecondQuestionBlockOfThisLetter();
            }
            this.occupiedOrientation = Orientation.BOTH;
            this.word2 = word;
        }
        return this;
    }

    private void setFirstQuestionBlockOfThisLetter(){
        this.firstQuestionBlockOfThisLetter = setQuestionBlock();
    }

    private void setSecondQuestionBlockOfThisLetter(){
        this.secondQuestionBlockOfThisLetter = setQuestionBlock();
    }

    private Letter setQuestionBlock(){
        final List<Letter> possibleQuestionBlocks = new ArrayList<>(3);
        if(occupiedOrientation == Orientation.HORIZONTAL){
            if(topLetter != NULL && topLetter.isEmpty){
                possibleQuestionBlocks.add(topLetter);
            }
            if(bottomLetter != NULL && bottomLetter.isEmpty){
                possibleQuestionBlocks.add(bottomLetter);
            }
            if(leftLetter != NULL && leftLetter.isEmpty){
                possibleQuestionBlocks.add(leftLetter);
            }
        }else { // Vertical
            if(topLetter != NULL && topLetter.isEmpty){
                possibleQuestionBlocks.add(topLetter);
            }
            if(rightLetter != NULL && rightLetter.isEmpty){
                possibleQuestionBlocks.add(rightLetter);
            }
            if(leftLetter != NULL && leftLetter.isEmpty){
                possibleQuestionBlocks.add(leftLetter);
            }
        }
        if(possibleQuestionBlocks.size() == 0){
            throw new RuntimeException("Shouldn't happen");
        }
        Letter letter = possibleQuestionBlocks.get(random.nextInt(possibleQuestionBlocks.size()));
        letter.isEmpty = false;
        letter.isQuestionBlock = true;
        return letter;
    }


    public boolean isHorizontal(){
        return occupiedOrientation == Orientation.HORIZONTAL || occupiedOrientation == Orientation.BOTH;
    }

    public boolean isVertical(){
        return occupiedOrientation == Orientation.VERTICAL || occupiedOrientation == Orientation.BOTH;
    }

    public void setEmpty() {
        this.isEmpty = true;
    }

    public boolean isEmpty(){
        return this.isEmpty;
    }

    public boolean isNotEmpty(){
        return !this.isEmpty;
    }

    static final char NULL_BLOCK = '▨';
    static final char EMPTY_BLOCK = '▢';
    static final char QUESTION_BLOCK = '�';
    public char getChar() {
        if (this == NULL) {
            return NULL_BLOCK;
        }
        if (isEmpty) {
            return EMPTY_BLOCK;
        }
        if (isQuestionBlock){
            return QUESTION_BLOCK;
        }
        return letter;
    }
}

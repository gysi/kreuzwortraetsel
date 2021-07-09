package de.gregord.kreuzwortraetsel;

import java.util.ArrayList;
import java.util.List;

public class Field implements Cloneable{
    private Letter[][] field;
    public final int width;
    public final int height;

    public Field(int width, int height, BlockedArea blockedArea) {
        this.width = width;
        this.height = height;
        this.field = new Letter[height][width];
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                Letter letter;
                if((row >= blockedArea.getPosx() && row <= blockedArea.getPosy()+ blockedArea.getHeight())
                    && (col >= blockedArea.getPosx() && col <= blockedArea.getPosx()+blockedArea.getWidth())){
                    letter = Letter.NULL;
                }else{
                    letter = new Letter();
                    letter.posX = col;
                    letter.posY = row;
                }
                field[row][col] = letter;
            }
        }

        setAdjacentFieldsToLetters(field);
    }

    private void setAdjacentFieldsToLetters(Letter[][] field){
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                if(field[row][col] == Letter.NULL){
                    continue;
                }
                field[row][col]
                        .addTop(row == 0 ? Letter.NULL : field[row - 1][col])
                        .addRight(col == field[0].length - 1 ? Letter.NULL : field[row][col + 1])
                        .addBottom(row == field.length - 1 ? Letter.NULL : field[row + 1][col])
                        .addLeft(col == 0 ? Letter.NULL : field[row][col - 1]);
            }
        }
    }

    public List<Letter> placeWord(String word, int posX, int posY, Orientation orientation) {
        List<Letter> placedLetters = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
//            try {
            Letter set = field[posY][posX].set(i == 0, word.charAt(i), word, orientation);
            placedLetters.add(set);
            //            }catch (RuntimeException e){
//                print();
//                System.out.println(word + " " + posX + " " + posY + " " + orientation);
//                throw e;
//            }
            if (orientation.equals(Orientation.HORIZONTAL)) {
                posX++;
            } else {
                posY++;
            }
        }
        return placedLetters;
    }

    public boolean isFieldEmpty() {
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                if (field[row][col].isNotEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Letter getLetter(int row, int col) {
        return field[row][col];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                sb.append(field[row][col].getChar()).append("  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void print() {
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                System.out.print(field[row][col].getChar() + " ");
            }
            System.out.println();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getEmptyFieldCount(){
        int count = 0;
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                if(field[row][col] != Letter.NULL && field[row][col].isEmpty){
                    count++;
                }
            }
        }
        return count;
    }

    public Letter[][] getField(){
        return this.field;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Field clone = (Field) super.clone();
        clone.field = new Letter[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                clone.field[row][col] = (Letter) this.field[row][col].clone();
            }
        }
        setAdjacentFieldsToLetters(clone.field);
        return clone;
    }
}

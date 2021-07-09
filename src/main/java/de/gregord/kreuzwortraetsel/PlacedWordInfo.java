package de.gregord.kreuzwortraetsel;

import java.util.List;

public record PlacedWordInfo(String word, int posx, int posy, char matchedChar, String matchedWord, List<Letter> letters){
    @Override
    public String toString() {
        return "PlacedWordInfo{" +
                "word='" + word + '\'' +
                ", posx=" + posx +
                ", posy=" + posy +
                ", matchedChar=" + matchedChar +
                ", matchedWord='" + matchedWord + '\'' +
                ", letters=" + letters +
                '}';
    }
}

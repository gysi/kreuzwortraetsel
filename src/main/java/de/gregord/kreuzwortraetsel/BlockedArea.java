package de.gregord.kreuzwortraetsel;

public class BlockedArea{
    private int posx;
    private int posy;
    private int width;
    private int height;

    public BlockedArea(){}

    public BlockedArea(int posx, int posy, int width, int height) {
        this.posx = posx;
        this.posy = posy;
        this.width = width;
        this.height = height;
    }

    public int getPosx() {
        return posx;
    }

    public void setPosx(int posx) {
        this.posx = posx;
    }

    public int getPosy() {
        return posy;
    }

    public void setPosy(int posy) {
        this.posy = posy;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

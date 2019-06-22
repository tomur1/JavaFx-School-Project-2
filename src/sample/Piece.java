package sample;

import javafx.geometry.Point2D;

public class Piece {
    Player color;
    Point2D location;
    boolean isQueen;

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    Piece(Player color, int x, int y, boolean isQueen){
        this.color = color;
        this.location = new Point2D(x,y);
        this.isQueen = isQueen;
    }



    public Player getColor() {
        return color;
    }

    public void setColor(Player color) {
        this.color = color;
    }

    public boolean isQueen() {
        return isQueen;
    }

    public void setQueen(boolean queen) {
        isQueen = queen;
    }
}

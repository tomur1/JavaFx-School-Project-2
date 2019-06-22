package sample;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class DrawingEngine {


    Pane pane;
    ImageView boardView;
    int pieceSize = 88;

    DrawingEngine(Pane pane, ImageView boardView){
        this.pane = pane;
        this.boardView = boardView;
    }

    //more like redraw board
    void drawBoard(ArrayList<Piece> piecesOnBoard, Piece selectedPiece){
        //first detach all
        ObservableList children = pane.getChildren();
        children.clear();
        children.add(boardView);
        for (Piece piece :
                piecesOnBoard) {
            Point2D position = getPiecePosition(piece);
            Image image;
            if (piece.color == Player.WHITE){
                if(piece.isQueen()){
                    if(piece == selectedPiece){
                        image = new Image("biala_dama_selected.png");
                    }else{
                        image = new Image("biala_dama.png");
                    }
                }else{
                    if(piece == selectedPiece){
                        image = new Image("bialy_pionek_selected.png");
                    }else{
                        image = new Image("bialy_pionek.png");
                    }

                }
            }else{
                if(piece.isQueen()){
                    if(piece == selectedPiece) {
                        image = new Image("czarna_dama_selected.png");
                    }else{
                        image = new Image("czarna_dama.png");
                    }
                }else{
                    if(piece == selectedPiece) {
                        image = new Image("pionek_selected.png");
                    }else{
                        image = new Image("pionek.png");
                    }

                }

            }
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(pieceSize);
            imageView.setX(position.getX());
            imageView.setY(position.getY());
            imageView.setMouseTransparent(true);
            children.add(imageView);
        }

    }

    Point2D getPiecePosition(Piece piece){
        int multiplayer = 88;
        return new Point2D((piece.getLocation().getX() * multiplayer) + 8, (piece.getLocation().getY()* multiplayer) + 8);
    }

    void highlightSquare(){

    }

}

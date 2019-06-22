package sample;

import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Engine {

    private DrawingEngine drawingEngine;
    ArrayList<Piece> piecesOnBoard;
    Piece selectedPiece;
    Player turn;
    Text turnText, capturedWhite, capturedBlack, timeLeftText;
    Slider slider;
    final int SCALE;
    boolean canChange = true;
    boolean lastTurnWasCapture = false;
    Thread turnClock;
    static int timeLeft;


    public Player getTurn() {
        return turn;
    }

    public void setTurn(Player turn) {
        this.turn = turn;
    }

    Engine(Pane root, ImageView boardview, int scale, Text text, Text white, Text black, Slider slider, Text timeLeftText) {
        piecesOnBoard = new ArrayList<>();
        turn = Player.WHITE;
        drawingEngine = new DrawingEngine(root, boardview);
        SCALE = scale;
        selectedPiece = null;
        turnText = text;
        capturedWhite = white;
        capturedBlack = black;
        this.slider = slider;
        this.timeLeftText = timeLeftText;


    }

    void newGame() {
        generateStartingPosition();
        drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
    }

    void clickRegistered(MouseEvent mouseClick) {
        Point2D boardPlace = convertPixelstoPosition(mouseClick.getX(), mouseClick.getY());
        System.out.println("Clicked X:" + boardPlace.getX() + "and Y: " + boardPlace.getY());
        if (swapSelectedPiece(boardPlace)) {
            return;
        }

        tryToMove(boardPlace);

    }

    void startClock(){
        // Create a Runnable
        timeLeft = (int) slider.getValue();

        Runnable task = () -> timer();
        if(turnClock != null && turnClock.isAlive()){
            return;
        }
        // Run the task in a background thread
        turnClock = new Thread(task);
        // Terminate the running thread if the application exits
        turnClock.setDaemon(true);
        // Start the thread
        turnClock.start();
    }

    void timer(){

        while(true){
            try {
                Thread.sleep((long) (1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeLeft--;
            timeLeftText.setText("time left: " + timeLeft);
            if(timeLeft == 0){
                timeLeftText.setText("The time has run out");
                break;
            }

        }

    }

    private void changeTurn() {
        if (turn == Player.WHITE) {
            turn = Player.BLACK;
            turnText.setText("Turn: Black");
        } else {
            turn = Player.WHITE;
            turnText.setText("Turn: White");
        }

        startClock();

    }

    void saveState(File file){
        //we need to save all the pieces and turn
        try {
            FileWriter fw = new FileWriter(file);
            if(turn == Player.WHITE){
                fw.write("white\n");
            }else{
                fw.write("black\n");
            }
            for (Piece piece :
                    piecesOnBoard) {
                fw.write((int)piece.getLocation().getX() + " " + (int)piece.getLocation().getY() + " " + piece.getColor() + " " + piece.isQueen());
                fw.write("\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadState(File file){
        try {
            Scanner scanner = new Scanner(file);
            if(scanner.next().equals("white")){
                turn = Player.WHITE;
                turnText.setText("Turn: White");
            }else{
                turn = Player.BLACK;
                turnText.setText("Turn: Black");
            }
            piecesOnBoard.clear();
            while(scanner.hasNext()){
                int x = Integer.parseInt(scanner.next());
                int y = Integer.parseInt(scanner.next());
                Player color;
                if(scanner.next().equals("WHITE")){
                    color = Player.WHITE;
                }else{
                    color = Player.BLACK;
                }
                boolean queen = scanner.nextBoolean();
                piecesOnBoard.add(new Piece(color,x,y,queen));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
    }


    boolean swapSelectedPiece(Point2D boardPlace) {
        if(!canChange){
            return false;
        }

        if (selectedPiece != pieceOnField(boardPlace) && pieceOnField(boardPlace) != null) {
            selectedPiece = pieceOnField(boardPlace);
            drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
            return true;
        }
        return false;
    }

    //top level moving logic
    void tryToMove(Point2D locationTo) {
        if (validateMove(locationTo)) {
            selectedPiece.setLocation(locationTo);
            if(selectedPiece.isQueen()){
                if(canQueenStillCapture() && lastTurnWasCapture){
                    //don't change turn and don't change selected piece
                    drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
                    canChange = false;
                    return;
                }else{
                    promoteIfNeeded();
                    drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
                    changeTurn();
                    canChange = true;
                    lastTurnWasCapture = false;
                }
            }else{
                if(canPieceStillCapture() && lastTurnWasCapture){
                    //don't change turn and don't change selected piece
                    drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
                    canChange = false;
                    return;
                }else{
                    promoteIfNeeded();
                    drawingEngine.drawBoard(piecesOnBoard, selectedPiece);
                    changeTurn();
                    canChange = true;
                    lastTurnWasCapture = false;
                }
            }

        }
        if(checkEnd()){
            System.out.println("Game Over");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(null);
            alert.setContentText("The game has ended. \n Please select new game or close the app");
            alert.showAndWait();
        }

    }

    boolean checkEnd() {
        Point2D stats = calculateStats();
        int white = (int)stats.getX();
        int black = (int)stats.getY();
        capturedWhite.setText("Pozostalo " + white + " bialych pionkow");
        capturedBlack.setText("Pozostalo " + black + " czarnych pionkow");
        if(white == 0){
            return true;
        }
        if(black == 0){
            return true;
        }
        return false;
    }

    Point2D calculateStats() {
        int white = 0;
        int black = 0;
        for (Piece piece :
                piecesOnBoard) {
            if(piece.color == Player.WHITE){
                white++;
            }else{
                black++;
            }
        }
        System.out.println("Bialych pionków zostalo:" + white);
        System.out.println("Czarnych pionków zostalo:" + black);
        return new Point2D(white,black);
    }

    void promoteIfNeeded() {
        for (Piece piece :
                piecesOnBoard) {
            if (piece.color == Player.BLACK && piece.getLocation().getY() == 0 && !piece.isQueen()) {
                System.out.println("Promoted");
                piece.setQueen(true);
            } else if (piece.color == Player.WHITE && piece.getLocation().getY() == 7 && !piece.isQueen()) {
                System.out.println("Promoted");
                piece.setQueen(true);
            }
        }
    }

    //return the piece that is on the given field. Return null if there is no such piece
    Piece pieceOnField(Point2D field) {
        for (Piece piece :
                piecesOnBoard) {
            if (piece.getLocation().getX() == field.getX() && piece.getLocation().getY() == field.getY()) {
                return piece;
            }
        }
        return null;
    }

    boolean onBoard(Point2D point) {
        if (point.getX() < 8 && point.getX() >= 0 && point.getY() < 8 && point.getY() >= 0) {
            return true;
        }
        return false;
    }

    boolean canMove(Point2D locationTo) {
        Point2D pieceLocation = selectedPiece.getLocation();
        Point2D moveVector = locationTo.subtract(pieceLocation);
        Point2D absMoveVector = new Point2D(Math.abs(moveVector.getX()), Math.abs(moveVector.getY()));

        if (absMoveVector.getX() != absMoveVector.getY()) {
            //They cannot be different. If they are that means that the move is invalid
            return false;
        }
        if(pieceOnField(locationTo) != null){
            //in checkers never can you place a piece on another piece
            return false;

        }

        if (selectedPiece.isQueen()) {
            return canQueenMove(pieceLocation, moveVector, absMoveVector, true, false);
        } else {
            if (absMoveVector.getX() == 1) {
                //it's just a normal move
                if (selectedPiece.color == Player.WHITE) {
                    if (moveVector.getY() == 1 && pieceOnField(locationTo) == null) {
                        return true;
                    }
                } else {
                    if (moveVector.getY() == -1 && pieceOnField(locationTo) == null) {
                        return true;
                    }
                }
            } else if (absMoveVector.getX() == 2) {
                return canPawnCapture(moveVector, true);
            }

        }
        return false;
    }

    private boolean canQueenMove(Point2D pieceLocation, Point2D moveVector, Point2D absMoveVector, boolean removePiece, boolean justCaptures) {
        //check if any square in the line is not empty. If it's not empty and if enemy is there then check if capture is possible.
        //otherwise move is invalid

        if(pieceOnField(pieceLocation.add(moveVector)) != null){
            return false;
        }

        int x = (int) moveVector.getX();
        int y = (int) moveVector.getY();

        if(x > 0){
            x = 1;
        }else{
            x = -1;
        }
        if(y > 0){
            y = 1;
        }else{
            y = -1;
        }

        int piecesOnLine = 0;
        Piece capturedPiece = null;
        //the function checks from piece toward clicked location
        for (int i = 0; i <absMoveVector.getX()-1; i++) {
            Piece pieceOnLine = pieceOnField(new Point2D(pieceLocation.getX() + x, pieceLocation.getY() + y));

            if(pieceOnLine != null){
                if(pieceOnLine.color == selectedPiece.color){
                    //cannot jump through your own pieces
                    return false;
                }else{
                    //there is an enemy piece on the line
                    capturedPiece = pieceOnLine;
                    piecesOnLine++;
                }

            }
            System.out.println("i: "+ i);

            if(x > 0){
                x += 1;
            }else{
                x += -1;
            }
            if(y > 0){
                y += 1;
            }else{
                y += -1;
            }

        }
        if(piecesOnLine == 0){
            //just go to selected place. Nothing is on the line
            if(justCaptures){
                return false;
            }
            return true;
        }else if(piecesOnLine == 1){
            //capture and go
            if(removePiece){
                lastTurnWasCapture = true;
                piecesOnBoard.remove(capturedPiece);
            }
            return true;
        }
        return false;
    }

    boolean canPieceStillCapture(){
        Point2D upRight = new Point2D(2,-2);
        Point2D upLeft = new Point2D(-2,-2);
        Point2D downRight = new Point2D(2,2);
        Point2D downLeft = new Point2D(-2,2);
        if(canPawnCapture(upRight, false) || canPawnCapture(upLeft, false) || canPawnCapture(downRight, false) || canPawnCapture(downLeft, false)){
            return true;
        }
        return false;
    }

    boolean canQueenStillCapture(){
        Point2D pieceLocation = selectedPiece.getLocation();
        //down left
        Point2D locationToDownRight = selectedPiece.location.add(2,2);
        while(onBoard(locationToDownRight)){
            Point2D moveVector = locationToDownRight.subtract(pieceLocation);
            Point2D absMoveVector = new Point2D(Math.abs(moveVector.getX()), Math.abs(moveVector.getY()));
            if (canQueenMove(pieceLocation, moveVector, absMoveVector, false, true)) {
                return true;
            }
            locationToDownRight = locationToDownRight.add(1,1);
        }

        pieceLocation = selectedPiece.getLocation();

        //down left
        Point2D locationToDownLeft = selectedPiece.location.add(-2,2);
        while(onBoard(locationToDownLeft)){
            Point2D moveVector = locationToDownLeft.subtract(pieceLocation);
            Point2D absMoveVector = new Point2D(Math.abs(moveVector.getX()), Math.abs(moveVector.getY()));
            if (canQueenMove(pieceLocation, moveVector, absMoveVector, false, true)) {
                return true;
            }
            locationToDownLeft = locationToDownLeft.add(-1,1);
        }

        pieceLocation = selectedPiece.getLocation();

        //down left
        Point2D locationToUpRight = selectedPiece.location.add(2,-2);
        while(onBoard(locationToUpRight)){
            Point2D moveVector = locationToUpRight.subtract(pieceLocation);
            Point2D absMoveVector = new Point2D(Math.abs(moveVector.getX()), Math.abs(moveVector.getY()));
            if (canQueenMove(pieceLocation, moveVector, absMoveVector, false, true)) {
                return true;
            }
            locationToUpRight = locationToUpRight.add(1,-1);
        }

        pieceLocation = selectedPiece.getLocation();

        //down left
        Point2D locationToUpLeft = selectedPiece.location.add(-2,-2);
        while(onBoard(locationToUpLeft)){
            Point2D moveVector = locationToUpLeft.subtract(pieceLocation);
            Point2D absMoveVector = new Point2D(Math.abs(moveVector.getX()), Math.abs(moveVector.getY()));
            if (canQueenMove(pieceLocation, moveVector, absMoveVector, false, true)) {
                return true;
            }
            locationToUpLeft = locationToUpLeft.add(-1,-1);
        }


        return false;
    }

    private boolean canPawnCapture(Point2D moveVector, boolean removePiece) {
        Point2D capturingLocation = moveVector.multiply(0.5).add(selectedPiece.location);
        Point2D finalLocation = moveVector.add(selectedPiece.location);
        Piece capturedPiece = pieceOnField(capturingLocation);

        if(!onBoard(finalLocation)){
            return false;
        }
        //if piece in the middle is different color and final place is free you can capture
        if (capturedPiece != null && capturedPiece.color != turn && pieceOnField(finalLocation) == null) {
            //capture is correct so take the pawn down
            if(removePiece){
                lastTurnWasCapture = true;
                piecesOnBoard.remove(capturedPiece);
            }
            return true;
        }
        return false;
    }


    //return false if move is not valid. True otherwise
    boolean validateMove(Point2D locationTo) {

        if (selectedPiece == null) {
            return false;
        }

        if (selectedPiece.getColor() == turn) {
            //turn is correct so it may be moved

            return canMove(locationTo);
        }
        return false;
    }


    Point2D convertPixelstoPosition(double x, double y) {
        int multiplayer = 88;
        return new Point2D((int) (x - 13) / (multiplayer), (int) (y - 13) / (multiplayer));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    void generateStartingPosition() {
        //reset list
        piecesOnBoard.clear();
        //generate white pieces
        int x = 0;
        int y = 0;
        for (int i = 0; i < 12; i++) {

            piecesOnBoard.add(new Piece(Player.WHITE, y, x, false));
            if (y < 6) {
                y = y + 2;
            } else {
                y = 0;
                x++;
                if (x % 2 == 1) {
                    y++;
                }
            }
        }

        //generate black pieces
        x = 5;
        y = 1;
        for (int i = 0; i < 12; i++) {

            piecesOnBoard.add(new Piece(Player.BLACK, y, x, false));
            if (y < 6) {
                y = y + 2;
            } else {
                y = 0;
                x++;
                if (x % 2 == 1) {
                    y++;
                }
            }
        }


    }
}

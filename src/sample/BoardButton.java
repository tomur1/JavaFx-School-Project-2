package sample;

import javafx.scene.control.Button;

public class BoardButton extends Button {

    final int x;
    final int y;

    public BoardButton(int x, int y) {
        this.x = x;
        this.y = y;
        this.setOnMouseClicked((e) -> {
            //make logic here
            //for now just sout
            System.out.println("field x: " + x + " y: " + y + " has been clicked");

        });
    }



}

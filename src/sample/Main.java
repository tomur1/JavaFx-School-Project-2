package sample;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    final int SCALE = 80;
    public static void main(String[] args) {
        launch(args);
    }
    Engine engine;

    @Override
    public void start(Stage primaryStage) {
        /*
        Hierarchy looks like this:
        Hbox splits in 2
        left:
        Pane
        right:
        Vbox
        */
        primaryStage.setTitle("Hello World!");

        HBox root = new HBox();

        //Left side of the application is a board

        Pane pane = new Pane();

        Image board = new Image("checkerboard.jpg");
        ImageView boardView = new ImageView(board);
        boardView.setFitWidth(SCALE * 9);
        boardView.setPreserveRatio(true);
        boardView.setOnMouseClicked((e) -> {engine.clickRegistered(e);});

        Text turn = new Text("Turn: White");


        //Right side
        FlowPane flowPane = new FlowPane();
        Text capturedWhite = new Text("");
        Text capturedBlack = new Text("");
        Text timeLeft = new Text("");

        Slider slider = new Slider();
        slider.setMin(5);
        slider.setMax(25);
        slider.setValue(5);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(5);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);


        engine = new Engine(pane, boardView, SCALE, turn, capturedWhite, capturedBlack, slider, timeLeft);

        VBox vBox = new VBox();
        vBox.setPrefWidth(SCALE * 7);
        vBox.setBackground(Background.EMPTY);
        String style = "-fx-background-color: rgba(0, 255, 255, 0.5);";
        vBox.setStyle(style);
        vBox.setAlignment(Pos.TOP_CENTER);

        Button newGame = new Button("New Game");
        newGame.setOnMouseClicked((e) -> { engine.newGame();
            System.out.println(root.getChildren().size());
        });

        Button saveGame = new Button("Save Game");
        saveGame.setOnMouseClicked((e) -> {saveState(primaryStage);});

        Button restoreGame = new Button("Restore Game");
        restoreGame.setOnMouseClicked((e) -> {restoreState(primaryStage);});

        vBox.setMargin(newGame, new Insets(8,8,8,8));
        vBox.setMargin(saveGame, new Insets(8,8,8,8));
        vBox.setMargin(restoreGame, new Insets(8,8,8,8));






        //Adding children
        flowPane.getChildren().add(capturedWhite);
        flowPane.getChildren().add(capturedBlack);
        flowPane.getChildren().add(turn);
        flowPane.getChildren().add(timeLeft);
        pane.getChildren().add(boardView);
        vBox.getChildren().add(newGame);
        vBox.getChildren().add(saveGame);
        vBox.getChildren().add(restoreGame);
        vBox.getChildren().add(slider);
        vBox.getChildren().add(flowPane);
        root.getChildren().add(pane);
        root.getChildren().add(vBox);

        primaryStage.setScene(new Scene(root, 16*SCALE, 9*SCALE));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    void saveState(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save State");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            engine.saveState(file);
        }
    }

    void restoreState(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load State");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            engine.loadState(file);
        }
    }
}
package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Vector Editor");

        primaryStage.setMaximized(true);

        InputStream iconStream = getClass().getResourceAsStream("/icon.png");
        assert iconStream != null;
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);

        Label helloWorldLabel = new Label();
        helloWorldLabel.setAlignment(Pos.CENTER);
        Scene primaryScene = new Scene(helloWorldLabel);
        primaryStage.setScene(primaryScene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
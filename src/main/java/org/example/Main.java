package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Загружаем FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/mainView.fxml"));
        Parent root = loader.load();

        // Настраиваем сцену
        Scene scene = new Scene(root);
        primaryStage.setTitle("Vector Editor");
        primaryStage.getIcons().add(new Image("/icon.png"));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
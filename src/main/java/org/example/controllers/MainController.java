package org.example.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javafx.stage.WindowEvent;
import org.example.enums.ToolMode;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private MenuItem newFileMenuItem;
    @FXML private MenuItem openFileMenuItem;
    @FXML private MenuItem saveFileMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;
    @FXML private MenuItem deleteMenuItem;
    @FXML private MenuItem zoomInMenuItem;
    @FXML private MenuItem zoomOutMenuItem;
    @FXML private MenuItem aboutMenuItem;

    @FXML private Button selectToolButton;
    @FXML private Button rectangleToolButton;
    @FXML private Button circleToolButton;
    @FXML private Button lineToolButton;
    @FXML private Button colorBlack;
    @FXML private Button colorRed;
    @FXML private Button colorBlue;
    @FXML private Button colorGreen;
    @FXML private Button colorYellow;
    @FXML private ColorPicker colorPicker;
    @FXML private Pane drawingArea;

    @FXML private Label statusBar;

    private ToolController toolController;
    private ColorController colorController;
    private DrawController drawController;
    private FileController fileController;

    private double scaleFactor = 1.0; // Текущий масштаб
    private static final double ZOOM_STEP = 0.1; // Шаг изменения масштаба
    private static final double MAX_SCALE = 5.0; // Максимальный масштаб
    private static final double MIN_SCALE = 0.5; // Минимальный масштаб

    private final EventHandler<WindowEvent> closeEventHandler = new javafx.event.EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
            if (DrawController.isModified()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Do you want to save your changes before exiting?");

                ButtonType saveButton = new ButtonType("Save");
                ButtonType discardButton = new ButtonType("Don't Save");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == saveButton) {
                        handleSaveFile();
                        System.exit(0);
                    } else if (result.get() == discardButton) {
                        System.exit(0);
                    } else if (result.get() == cancelButton) {
                        event.consume();
                    }
                } else {
                    event.consume();
                }
            } else {
                System.exit(0);
            }
        }
    };

    public EventHandler<WindowEvent> getCloseEventHandler(){
        return closeEventHandler;
    }

    @FXML
    public void initialize() {
        toolController = new ToolController();
        colorController = new ColorController();
        drawController = new DrawController();
        fileController = new FileController();

        // Установка обработчиков для кнопок инструментов
        selectToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.SELECT);
            statusBar.setText("Tool: Select");
        });
        rectangleToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.RECTANGLE);
            statusBar.setText("Tool: Rectangle");
        });
        circleToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.CIRCLE);
            statusBar.setText("Tool: Circle");
        });
        lineToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.LINE);
            statusBar.setText("Tool: Line");
        });

        //Изменение цвета
        colorBlack.setOnAction(event -> {
            colorController.setCurrentColor(Color.BLACK);
            statusBar.setText("Color: Black");
        });
        colorRed.setOnAction(event -> {
            colorController.setCurrentColor(Color.RED);
            statusBar.setText("Color: Red");
        });
        colorBlue.setOnAction(event -> {
            colorController.setCurrentColor(Color.BLUE);
            statusBar.setText("Color: Blue");
        });
        colorGreen.setOnAction(event -> {
            colorController.setCurrentColor(Color.GREEN);
            statusBar.setText("Color: Green");
        });
        colorYellow.setOnAction(event -> {
            colorController.setCurrentColor(Color.YELLOW);
            statusBar.setText("Color: Yellow");
        });

        // Установка обработчика для ColorPicker
        colorPicker.setOnAction(event -> {
            Color selectedColor = colorPicker.getValue();
            colorController.setCurrentColor(selectedColor);
            statusBar.setText("Color: " + selectedColor.toString());
        });

        // Инициализация DrawController
        drawController.initialize(drawingArea, toolController, colorController);
        fileController.initialize(drawController);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(drawingArea.widthProperty());
        clip.heightProperty().bind(drawingArea.heightProperty());
        drawingArea.setClip(clip);

        drawController.resetModificationStatus();

        // Установка обработчиков для меню
        newFileMenuItem.setOnAction(event -> handleNewFile());
        openFileMenuItem.setOnAction(event -> handleOpenFile());
        saveFileMenuItem.setOnAction(event -> handleSaveFile());
        drawingArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown() && event.getCode().toString().equals("S")) {
                        handleSaveFile();
                        statusBar.setText("File saved using Ctrl+S.");
                        event.consume();
                    }
                });
            }
        });

        exitMenuItem.setOnAction(event -> handleExit());

        undoMenuItem.setOnAction(event -> statusBar.setText("Undo: Not implemented yet."));
        redoMenuItem.setOnAction(event -> statusBar.setText("Redo: Not implemented yet."));
        deleteMenuItem.setOnAction(event -> statusBar.setText("Delete: Not implemented yet."));

        zoomInMenuItem.setOnAction(event -> {
            if (scaleFactor < MAX_SCALE) {
                scaleFactor += ZOOM_STEP;
                applyZoom();
                statusBar.setText("Zoom In: " + (int) (scaleFactor * 100) + "%");
            } else {
                statusBar.setText("Maximum zoom level reached.");
            }
        });

        // Zoom Out
        zoomOutMenuItem.setOnAction(event -> {
            if (scaleFactor > MIN_SCALE) {
                scaleFactor -= ZOOM_STEP;
                applyZoom();
                statusBar.setText("Zoom Out: " + (int) (scaleFactor * 100) + "%");
            } else {
                statusBar.setText("Minimum zoom level reached.");
            }
        });

        drawingArea.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) { // Прокрутка вверх
                    if (scaleFactor < MAX_SCALE) {
                        scaleFactor += ZOOM_STEP;
                        applyZoom();
                        statusBar.setText("Zoom In: " + (int) (scaleFactor * 100) + "%");
                    } else {
                        statusBar.setText("Maximum zoom level reached.");
                    }
                } else if (event.getDeltaY() < 0) { // Прокрутка вниз
                    if (scaleFactor > MIN_SCALE) {
                        scaleFactor -= ZOOM_STEP;
                        applyZoom();
                        statusBar.setText("Zoom Out: " + (int) (scaleFactor * 100) + "%");
                    } else {
                        statusBar.setText("Minimum zoom level reached.");
                    }
                }
                event.consume();
            }
        });

        aboutMenuItem.setOnAction(event -> statusBar.setText("About: Vector Editor v1.0"));
    }

    private void handleNewFile() {
        if (DrawController.isModified()) {
            // Показать диалог с вопросом о сохранении
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before opening a new file?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                } else if (result.get() == discardButton) {
                    drawController.clearCanvas();
                    statusBar.setText("New file created.");
                }
            }
        } else {
            drawController.clearCanvas();
            statusBar.setText("New file created.");
        }

    }

    private void handleOpenFile() {
        // Создаём список форматов
        List<String> choices = List.of("JSON", "SVG");

        // Создаём диалог выбора формата
        ChoiceDialog<String> dialog = new ChoiceDialog<>("JSON", choices);
        dialog.setTitle("Open File");
        dialog.setHeaderText("Choose a file format");
        dialog.setContentText("Format:");

        // Показываем диалог
        dialog.showAndWait().ifPresent(format -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");

            // Настройка фильтра расширений в зависимости от формата
            switch (format) {
                case "JSON":
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                    break;
                case "SVG":
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
                    break;
            }

            // Показываем диалог выбора файла
            File file = fileChooser.showOpenDialog(drawingArea.getScene().getWindow());
            if (file != null) {
                try {
                    if ("JSON".equals(format)) {
                        fileController.loadFromJSON(file); // Загрузка JSON
                    } else if ("SVG".equals(format)) {
                        fileController.loadFromSvg(file); // Загрузка SVG
                    }
                    drawController.resetModificationStatus();
                    statusBar.setText("File loaded: " + file.getName() + " as " + format);
                } catch (IOException | ParseException e) {
                    statusBar.setText("Failed to load file: " + e.getMessage());
                }
            }
        });
    }

    private void handleSaveFile() {
        // Создаём список форматов
        List<String> choices = List.of("JSON", "SVG", "PNG");

        // Создаём диалог выбора формата
        ChoiceDialog<String> dialog = new ChoiceDialog<>("JSON", choices);
        dialog.setTitle("Save File");
        dialog.setHeaderText("Choose a file format");
        dialog.setContentText("Format:");

        // Показываем диалог
        dialog.showAndWait().ifPresent(format -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            switch (format) {
                case "JSON":
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
                    break;
                case "SVG":
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));
                    break;
                case "PNG":
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
                    break;
            }

            File file = fileChooser.showSaveDialog(drawingArea.getScene().getWindow());
            if (file != null) {
                try {
                    if ("JSON".equals(format)) {
                        fileController.saveToJSON(file);
                    } else if ("SVG".equals(format)) {
                        fileController.saveToSvg(file);
                    } else if ("PNG".equals(format)) {
                        fileController.saveToPNG(file);
                    }
                    drawController.resetModificationStatus();
                    statusBar.setText("File saved: " + file.getName() + " as " + format);
                } catch (IOException e) {
                    statusBar.setText("Failed to save file: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleExit() {
        if (DrawController.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before exiting?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                } else if (result.get() == discardButton) {
                    // Закрыть приложение
                    System.exit(0);
                }
            }
        } else {
            // Если изменений нет, просто закрыть приложение
            System.exit(0);
        }
    }

    private void applyZoom() {
        drawController.getContentGroup().setScaleX(scaleFactor);
        drawController.getContentGroup().setScaleY(scaleFactor);
    }

}

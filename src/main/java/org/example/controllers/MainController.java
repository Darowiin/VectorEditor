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
    @FXML private Button ellipseToolButton;
    @FXML private Button lineToolButton;
    @FXML private Button curveToolButton;
    @FXML private Button colorBlack;
    @FXML private Button colorRed;
    @FXML private Button colorBlue;
    @FXML private Button colorGreen;
    @FXML private Button colorYellow;
    @FXML private ColorPicker colorPicker;
    @FXML private ColorPicker fillColorPicker;
    @FXML private Slider strokeWidthSlider;
    @FXML private Label strokeWidthValueLabel;

    @FXML private ScrollPane drawingScrollPane;

    @FXML private Pane drawingArea;

    @FXML private Label statusBar;

    private ToolController toolController;
    private ColorController colorController;
    private DrawController drawController;
    private FileController fileController;

    private double scaleFactor = 1.0; // Текущий масштаб
    private static final double ZOOM_STEP = 0.1; // Шаг изменения масштаба
    private static final double MAX_SCALE = 5.0; // Максимальный масштаб
    private static final double MIN_SCALE = 1.0; // Минимальный масштаб

    private final EventHandler<WindowEvent> closeEventHandler = event -> {
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

        selectToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.SELECT);
            statusBar.setText("Tool: Select");
        });
        rectangleToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.RECTANGLE);
            statusBar.setText("Tool: Rectangle");
        });
        ellipseToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.ELLIPSE);
            statusBar.setText("Tool: Circle");
        });
        lineToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.LINE);
            statusBar.setText("Tool: Line");
        });
        curveToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.CURVE);
            statusBar.setText("Tool: Curve");
        });

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

        colorPicker.setOnAction(event -> {
            Color selectedColor = colorPicker.getValue();
            colorController.setCurrentColor(selectedColor);
            statusBar.setText("Color: " + selectedColor.toString());
        });
        this.fillColorPicker.setValue(colorController.getFillColor());
        this.fillColorPicker.setOnAction(event ->
                colorController.setFillColor(fillColorPicker.getValue())
        );

        drawController.initialize(drawingArea, toolController, colorController);
        fileController.initialize(drawController);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(drawingArea.widthProperty());
        clip.heightProperty().bind(drawingArea.heightProperty());
        drawingArea.setClip(clip);

        drawController.resetModificationStatus();

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
                    } else if (event.isControlDown() && event.getCode().toString().equals("O")) {
                        handleOpenFile();
                        statusBar.setText("File opened using Ctrl+O.");
                        event.consume();
                    } else if (event.isControlDown() && event.getCode().toString().equals("N")) {
                        handleNewFile();
                        statusBar.setText("New file created using Ctrl+N.");
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
                if (event.getDeltaY() > 0) {
                    if (scaleFactor < MAX_SCALE) {
                        scaleFactor += ZOOM_STEP;
                        applyZoom();
                        statusBar.setText("Zoom In: " + (int) (scaleFactor * 100) + "%");
                    } else {
                        statusBar.setText("Maximum zoom level reached.");
                    }
                } else if (event.getDeltaY() < 0) {
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

        strokeWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double strokeWidth = newValue.doubleValue();
            strokeWidthValueLabel.setText(String.format("%.1f", strokeWidth));

            // Применить ширину к текущему инструменту или выделенной фигуре
            drawController.setStrokeWidth(strokeWidth);
            statusBar.setText("Stroke width: " + strokeWidth);
        });

        aboutMenuItem.setOnAction(event -> statusBar.setText("About: Vectorium v1.0"));
    }

    private void handleNewFile() {
        if (DrawController.isModified()) {
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
                    drawController.resetModificationStatus();
                    statusBar.setText("New file created.");
                }
            }
        } else {
            drawController.clearCanvas();
            drawController.resetModificationStatus();
            statusBar.setText("New file created.");
        }

    }

    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        // Добавление фильтров для выбора расширений
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("SVG Files", "*.svg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Показ диалогового окна выбора файла
        File file = fileChooser.showOpenDialog(drawingArea.getScene().getWindow());
        if (file != null) {
            try {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json")) {
                    fileController.loadFromJSON(file);
                } else if (fileName.endsWith(".svg")) {
                    fileController.loadFromSvg(file);
                } else {
                    throw new IllegalArgumentException("Unsupported file format: " + file.getName());
                }
                drawController.resetModificationStatus();
                statusBar.setText("File loaded: " + file.getName());
            } catch (IOException | ParseException | IllegalArgumentException e) {
                statusBar.setText("Failed to load file: " + e.getMessage());
            }
        }
    }

    private void handleSaveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        // Добавление фильтров для выбора расширений
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("SVG Files", "*.svg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        // Показ диалогового окна сохранения файла
        File file = fileChooser.showSaveDialog(drawingArea.getScene().getWindow());
        if (file != null) {
            try {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json")) {
                    fileController.saveToJSON(file);
                } else if (fileName.endsWith(".svg")) {
                    fileController.saveToSvg(file);
                } else if (fileName.endsWith(".png")) {
                    fileController.saveToPNG(file);
                } else {
                    throw new IllegalArgumentException("Unsupported file format: " + file.getName());
                }
                drawController.resetModificationStatus();
                statusBar.setText("File saved: " + file.getName());
            } catch (IOException | IllegalArgumentException e) {
                statusBar.setText("Failed to save file: " + e.getMessage());
            }
        }
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
                    System.exit(0);
                }
            }
        } else {
            System.exit(0);
        }
    }

    private void applyZoom() {
        drawController.getContentGroup().setScaleX(scaleFactor);
        drawController.getContentGroup().setScaleY(scaleFactor);
    }

}

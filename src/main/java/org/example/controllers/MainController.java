package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import org.example.enums.ToolMode;

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

    @FXML
    public void initialize() {
        toolController = new ToolController();
        colorController = new ColorController();
        drawController = new DrawController();

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

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(drawingArea.widthProperty());
        clip.heightProperty().bind(drawingArea.heightProperty());
        drawingArea.setClip(clip);

        // Установка обработчиков для меню
        newFileMenuItem.setOnAction(event -> handleNewFile());
        openFileMenuItem.setOnAction(event -> statusBar.setText("Open File: Not implemented yet."));
        saveFileMenuItem.setOnAction(event -> statusBar.setText("Save File: Not implemented yet."));
        exitMenuItem.setOnAction(event -> System.exit(0));

        undoMenuItem.setOnAction(event -> statusBar.setText("Undo: Not implemented yet."));
        redoMenuItem.setOnAction(event -> statusBar.setText("Redo: Not implemented yet."));
        deleteMenuItem.setOnAction(event -> statusBar.setText("Delete: Not implemented yet."));

        zoomInMenuItem.setOnAction(event -> statusBar.setText("Zoom In: Not implemented yet."));
        zoomOutMenuItem.setOnAction(event -> statusBar.setText("Zoom Out: Not implemented yet."));

        aboutMenuItem.setOnAction(event -> statusBar.setText("About: Vector Editor v1.0"));
    }

    private void handleNewFile() {
        drawController.clearCanvas();
        statusBar.setText("New file created.");
    }
}

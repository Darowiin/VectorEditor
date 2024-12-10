package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

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

    @FXML private TextField shapeColorField;
    @FXML private AnchorPane drawingArea;

    @FXML private Label statusBar;

    private ToolMode currentTool = ToolMode.SELECT;

    // Временные переменные для рисования
    private Rectangle currentRectangle;
    private Circle currentCircle;
    private Line currentLine;
    private double startX, startY;

    @FXML
    public void initialize() {
        newFileMenuItem.setOnAction(event -> handleNewFile());
        exitMenuItem.setOnAction(event -> System.exit(0));

        // Настройка инструментов
        selectToolButton.setOnAction(event -> setToolMode(ToolMode.SELECT));
        rectangleToolButton.setOnAction(event -> setToolMode(ToolMode.RECTANGLE));
        circleToolButton.setOnAction(event -> setToolMode(ToolMode.CIRCLE));
        lineToolButton.setOnAction(event -> setToolMode(ToolMode.LINE));

        // Настройка событий мыши только для drawingArea
        drawingArea.setOnMousePressed(this::handleMousePressed);
        drawingArea.setOnMouseDragged(this::handleMouseDragged);
        drawingArea.setOnMouseReleased(this::handleMouseReleased);

        // Установка цвета по умолчанию
        shapeColorField.setText("#000000"); // Черный цвет по умолчанию
    }

    private void handleNewFile() {
        statusBar.setText("Creating new file...");
        drawingArea.getChildren().clear(); // Очистка холста
    }

    private void setToolMode(ToolMode tool) {
        currentTool = tool;
        statusBar.setText("Tool selected: " + tool);
    }

    private Color getSelectedColor() {
        try {
            return Color.web(shapeColorField.getText().trim());
        } catch (IllegalArgumentException e) {
            statusBar.setText("Invalid color format! Using default #000000.");
            return Color.BLACK;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (!isInsideDrawingArea(event.getX(), event.getY())) return;

        startX = event.getX();
        startY = event.getY();

        if (currentTool == ToolMode.RECTANGLE) {
            currentRectangle = new Rectangle(startX, startY, 0, 0);
            currentRectangle.setFill(Color.TRANSPARENT);
            currentRectangle.setStroke(getSelectedColor());
            currentRectangle.setStrokeWidth(2);
            drawingArea.getChildren().add(currentRectangle);
        } else if (currentTool == ToolMode.CIRCLE) {
            currentCircle = new Circle(startX, startY, 0);
            currentCircle.setFill(Color.TRANSPARENT);
            currentCircle.setStroke(getSelectedColor());
            currentCircle.setStrokeWidth(2);
            drawingArea.getChildren().add(currentCircle);
        } else if (currentTool == ToolMode.LINE) {
            currentLine = new Line(startX, startY, startX, startY);
            currentLine.setStroke(getSelectedColor());
            currentLine.setStrokeWidth(2);
            drawingArea.getChildren().add(currentLine);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double endX = clampToDrawingArea(event.getX(), true);
        double endY = clampToDrawingArea(event.getY(), false);

        if (currentTool == ToolMode.RECTANGLE && currentRectangle != null) {
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            currentRectangle.setWidth(width);
            currentRectangle.setHeight(height);

            if (endX < startX) currentRectangle.setX(endX);
            if (endY < startY) currentRectangle.setY(endY);
        } else if (currentTool == ToolMode.CIRCLE && currentCircle != null) {
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            currentCircle.setRadius(radius);
        } else if (currentTool == ToolMode.LINE && currentLine != null) {
            currentLine.setEndX(endX);
            currentLine.setEndY(endY);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (currentTool == ToolMode.RECTANGLE) {
            statusBar.setText("Rectangle drawn.");
            currentRectangle = null;
        } else if (currentTool == ToolMode.CIRCLE) {
            statusBar.setText("Circle drawn.");
            currentCircle = null;
        } else if (currentTool == ToolMode.LINE) {
            statusBar.setText("Line drawn.");
            currentLine = null;
        }
    }

    private boolean isInsideDrawingArea(double x, double y) {
        return x >= 0 && y >= 0 && x <= drawingArea.getWidth() && y <= drawingArea.getHeight();
    }

    private double clampToDrawingArea(double value, boolean isX) {
        if (isX) {
            return Math.max(0, Math.min(value, drawingArea.getWidth()));
        } else {
            return Math.max(0, Math.min(value, drawingArea.getHeight()));
        }
    }
}

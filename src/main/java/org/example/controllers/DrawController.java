package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import org.example.enums.ToolMode;

public class DrawController {
    @FXML private Pane drawingArea;            // Холст для рисования
    private ToolController toolController; // Контроллер инструментов
    private ColorController colorController; // Контроллер цветов

    private Rectangle currentRectangle;
    private Circle currentCircle;
    private Line currentLine;

    private double startX, startY;

    public void initialize(Pane drawingArea, ToolController toolController, ColorController colorController) {
        this.drawingArea = drawingArea;
        this.toolController = toolController;
        this.colorController = colorController;

        drawingArea.setOnMousePressed(this::handleMousePressed);
        drawingArea.setOnMouseDragged(this::handleMouseDragged);
        drawingArea.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();

        ToolMode currentTool = toolController.getCurrentTool();
        Color currentColor = colorController.getCurrentColor();

        if (currentTool == ToolMode.RECTANGLE) {
            currentRectangle = new Rectangle(startX, startY, 0, 0);
            currentRectangle.setFill(Color.TRANSPARENT);
            currentRectangle.setStroke(currentColor);
            currentRectangle.setStrokeWidth(2);
            drawingArea.getChildren().add(currentRectangle);
        } else if (currentTool == ToolMode.CIRCLE) {
            currentCircle = new Circle(startX, startY, 0);
            currentCircle.setFill(Color.TRANSPARENT);
            currentCircle.setStroke(currentColor);
            currentCircle.setStrokeWidth(2);
            drawingArea.getChildren().add(currentCircle);
        } else if (currentTool == ToolMode.LINE) {
            currentLine = new Line(startX, startY, startX, startY);
            currentLine.setStroke(currentColor);
            currentLine.setStrokeWidth(2);
            drawingArea.getChildren().add(currentLine);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double endX = event.getX();
        double endY = event.getY();

        if (currentRectangle != null) {
            currentRectangle.setWidth(Math.abs(endX - startX));
            currentRectangle.setHeight(Math.abs(endY - startY));
            if (endX < startX) currentRectangle.setX(endX);
            if (endY < startY) currentRectangle.setY(endY);
        } else if (currentCircle != null) {
            double radius = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            currentCircle.setRadius(radius);
        } else if (currentLine != null) {
            currentLine.setEndX(endX);
            currentLine.setEndY(endY);
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (currentRectangle != null) {
            currentRectangle = null;
        } else if (currentCircle != null) {
            currentCircle = null;
        } else if (currentLine != null) {
            currentLine = null;
        }
    }

    public void clearCanvas() {
        drawingArea.getChildren().clear();
    }
}

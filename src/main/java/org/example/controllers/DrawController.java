package org.example.controllers;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import org.example.enums.ToolMode;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.shape.*;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

import java.util.*;


public class DrawController {
    @FXML Pane drawingArea;
    protected Group contentGroup;
    private ToolController toolController;
    private ColorController colorController;
    private HistoryController historyController;
    private ResizingController resizingController;

    private Rectangle selectionRectangle;
    private Rectangle currentRectangle;
    private Ellipse currentEllipse;
    private Line currentLine;
    private Path currentCurve;
    private Polygon currentPolygon;

    private double startX, startY;
    private static boolean isModified = false;
    private boolean isDraggingSelectedShapes = false; // Флаг для перетаскивания
    private boolean isSelecting = false;             // Флаг для выделения области
    private boolean isDrawing = false;               // Флаг для рисования
    private double selectionStartX, selectionStartY; // Координаты начала выделения
    protected double currentStrokeWidth = 2.0;
    private List<Shape> selectedShapes = new ArrayList<>();
    private boolean isEditingText = false; // Флаг редактирования текста

    public void initialize(Pane drawingArea, ToolController toolController, ColorController colorController, HistoryController historyController, ResizingController resizingController) {
        this.drawingArea = drawingArea;
        this.toolController = toolController;
        this.colorController = colorController;
        this.historyController = historyController;
        this.resizingController = resizingController;

        this.contentGroup = new Group();
        drawingArea.getChildren().add(contentGroup);

        activateDrawingHandlers();
    }

    private void addTextArea(double x, double y) {
        TextArea textArea = new TextArea();
        textArea.setLayoutX(x);
        textArea.setLayoutY(y);
        textArea.setPrefWidth(300);
        textArea.setPrefHeight(100);
        textArea.setWrapText(true);

        textArea.setFont(Font.font(MainController.fontSizeValue));

        contentGroup.getChildren().add(textArea);
        Platform.runLater(textArea::requestFocus);

        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                finalizeTextArea(x, y, textArea);
            }
        });

        textArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                finalizeTextArea(x, y, textArea);
            }
        });
    }

    private void finalizeTextArea(double x, double y, TextArea textArea) {
        if (!contentGroup.getChildren().contains(textArea)) return;

        String text = textArea.getText();
        Text displayText = new Text(x, y, text);
        displayText.setFill(colorController.getFillColor());
        displayText.setFont(Font.font(MainController.fontSizeValue));

        contentGroup.getChildren().add(displayText);

        historyController.addAction(
                () -> {
                    contentGroup.getChildren().remove(displayText);
                    if (!contentGroup.getChildren().contains(textArea)) {
                        contentGroup.getChildren().add(textArea);
                        Platform.runLater(() -> textArea.requestFocus());
                    }
                },
                () -> {
                    contentGroup.getChildren().remove(textArea);
                    contentGroup.getChildren().add(displayText);
                }
        );

        contentGroup.getChildren().remove(textArea);
    }

    public void addShape(Shape shape) {
        if (!contentGroup.getChildren().contains(shape)) {
            contentGroup.getChildren().add(shape);
            historyController.addAction(
                    () -> {
                        contentGroup.getChildren().remove(shape);
                        resizingController.removeHandlesFromShape(shape);
                    },
                    () -> {
                        contentGroup.getChildren().add(shape);
                        resizingController.enableResizing(shape);
                    }
            );
            System.out.println("Shape added.");
        }
    }

    public void activateDrawingHandlers() {
        Map<Shape, Point2D> initialPositions = new HashMap<>();
        Map<Shape, Point2D> finalPositions = new HashMap<>();

        drawingArea.setOnMousePressed(event -> {
            ToolMode currentTool = toolController.getCurrentTool();
            double localX = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
            double localY = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

            if (currentTool == ToolMode.AREA) {
                isSelecting = true;
                selectionStartX = localX;
                selectionStartY = localY;

                selectionRectangle = new Rectangle(selectionStartX, selectionStartY, 0, 0);
                selectionRectangle.setFill(Color.LIGHTBLUE.deriveColor(0, 1.0, 1.0, 0.3));
                selectionRectangle.setStroke(Color.BLUE);
                selectionRectangle.setStrokeWidth(1);
                contentGroup.getChildren().add(selectionRectangle);
            } else if (!selectedShapes.isEmpty() && currentTool == ToolMode.MOVE) {
                startX = event.getSceneX();
                startY = event.getSceneY();
                isDraggingSelectedShapes = true;
                drawingArea.setCursor(Cursor.MOVE);
                initialPositions.clear();
                for (Shape shape : selectedShapes) {
                    initialPositions.put(shape, new Point2D(shape.getTranslateX(), shape.getTranslateY()));
                }
            } else if (currentTool == ToolMode.RECTANGLE ||
                    currentTool == ToolMode.ELLIPSE ||
                    currentTool == ToolMode.LINE ||
                    currentTool == ToolMode.CURVE ||
                    currentTool == ToolMode.POLYGON ||
                    currentTool == ToolMode.TEXT
            ) {
                startX = localX;
                startY = localY;
                isDrawing = true;

                if (currentTool == ToolMode.RECTANGLE) {
                    currentRectangle = new Rectangle(startX, startY, 0, 0);
                    currentRectangle.setFill(colorController.getFillColor());
                    currentRectangle.setStroke(colorController.getCurrentColor());
                    currentRectangle.setStrokeWidth(currentStrokeWidth);
                    addShape(currentRectangle);
                } else if (currentTool == ToolMode.ELLIPSE) {
                    currentEllipse = new Ellipse(startX, startY, 0, 0);
                    currentEllipse.setFill(colorController.getFillColor());
                    currentEllipse.setStroke(colorController.getCurrentColor());
                    currentEllipse.setStrokeWidth(currentStrokeWidth);
                    addShape(currentEllipse);
                } else if (currentTool == ToolMode.LINE) {
                    currentLine = new Line(startX, startY, startX, startY);
                    currentLine.setStroke(colorController.getCurrentColor());
                    currentLine.setStrokeWidth(currentStrokeWidth);
                    addShape(currentLine);
                } else if (currentTool == ToolMode.CURVE) {
                    currentCurve = new Path();
                    currentCurve.setStroke(colorController.getCurrentColor());
                    currentCurve.setStrokeWidth(currentStrokeWidth);
                    currentCurve.setFill(colorController.getFillColor());

                    MoveTo moveTo = new MoveTo(startX, startY);
                    currentCurve.getElements().add(moveTo);
                    addShape(currentCurve);
                } else if (currentTool == ToolMode.POLYGON) {
                    if (event.isSecondaryButtonDown()) {
                        resizingController.enableResizing(currentPolygon);
                        currentPolygon = null;

                    } else if (currentPolygon == null) {
                        currentPolygon = new Polygon();
                        currentPolygon.setStroke(colorController.getCurrentColor());
                        currentPolygon.setStrokeWidth(currentStrokeWidth);
                        currentPolygon.setFill(colorController.getFillColor());

                        addShape(currentPolygon);
                    }
                    currentPolygon.getPoints().addAll(localX, localY);
                } else if (currentTool == ToolMode.TEXT) {
                    addTextArea(startX, startY);
                    isEditingText = true;
                }
                markAsModified();
            }
        });

        drawingArea.setOnMouseDragged(event -> {
            double localX = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
            double localY = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

            if (isSelecting && selectionRectangle != null) {
                double width = Math.abs(localX - selectionStartX);
                double height = Math.abs(localY - selectionStartY);

                selectionRectangle.setX(Math.min(selectionStartX, localX));
                selectionRectangle.setY(Math.min(selectionStartY, localY));
                selectionRectangle.setWidth(width);
                selectionRectangle.setHeight(height);
            } else if (isDraggingSelectedShapes) {
                double deltaX = event.getSceneX() - startX;
                double deltaY = event.getSceneY() - startY;

                moveSelectedShapes(deltaX, deltaY);

                startX = event.getSceneX();
                startY = event.getSceneY();
                markAsModified();
            } else if (isDrawing) {
                if (currentRectangle != null) {
                    currentRectangle.setWidth(Math.abs(localX - startX));
                    currentRectangle.setHeight(Math.abs(localY - startY));
                    if (localX < startX) currentRectangle.setX(localX);
                    if (localY < startY) currentRectangle.setY(localY);
                } else if (currentEllipse != null) {
                    currentEllipse.setRadiusX(Math.abs(localX - startX));
                    currentEllipse.setRadiusY(Math.abs(localY - startY));
                } else if (currentLine != null) {
                    currentLine.setEndX(localX);
                    currentLine.setEndY(localY);
                } else if (currentCurve != null) {
                    PathElement lastElement = currentCurve.getElements().get(currentCurve.getElements().size() - 1);
                    if (lastElement instanceof QuadCurveTo quadCurve) {
                        quadCurve.setControlX(localX);
                        quadCurve.setControlY(localY);
                    } else {
                        QuadCurveTo quadCurveTo = new QuadCurveTo(startX, startY, localX, localY);
                        currentCurve.getElements().add(quadCurveTo);
                    }
                } else if (currentPolygon != null) {
                    int size = currentPolygon.getPoints().size();
                    if (size >= 2) {
                        currentPolygon.getPoints().set(size - 2, localX);
                        currentPolygon.getPoints().set(size - 1, localY);
                    }
                }
            }
        });

        drawingArea.setOnMouseReleased(event -> {
            if (isSelecting && selectionRectangle != null) {
                Bounds selectionBounds = selectionRectangle.getBoundsInParent();
                selectedShapes.clear();
                for (javafx.scene.Node node : contentGroup.getChildren()) {
                    if (node instanceof Shape shape && shape.getBoundsInParent().intersects(selectionBounds) && !Objects.equals(node.getId(), "draggable-handle")) {
                        selectedShapes.add(shape);
                    }
                }
                contentGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
                isSelecting = false;
            } else if (isDraggingSelectedShapes) {
                isDraggingSelectedShapes = false;
                drawingArea.setCursor(Cursor.DEFAULT);
                finalPositions.clear();
                for (Shape shape : selectedShapes) {
                    finalPositions.put(shape, new Point2D(shape.getTranslateX(), shape.getTranslateY()));

                    resizingController.updateShapePosition(shape);
                    resizingController.removeHandlesFromShape(shape);
                    resizingController.enableResizing(shape);
                }

                selectedShapes.clear();
                markAsModified();
            } else if (isDrawing) {
                isDrawing = false;
                if (currentRectangle != null) {
                    resizingController.enableResizing(currentRectangle);
                    currentRectangle = null;
                } else if (currentEllipse != null) {
                    resizingController.enableResizing(currentEllipse);
                    currentEllipse = null;
                } else if (currentCurve != null) {
                    resizingController.enableResizing(currentCurve);
                    currentCurve = null;
                } else if (currentLine != null) {
                    resizingController.enableResizing(currentLine);
                    currentLine = null;
                } else if (currentPolygon != null && event.getClickCount() == 2) {
                    resizingController.enableResizing(currentPolygon);
                    currentPolygon = null;
                }
                markAsModified();
            }
        });
    }

    public void deactivateDrawingHandlers() {
        drawingArea.setOnMousePressed(null);
        drawingArea.setOnMouseDragged(null);
        drawingArea.setOnMouseReleased(null);
    }

    private void moveSelectedShapes(double deltaX, double deltaY) {
        for (Shape shape : selectedShapes) {
            shape.setTranslateX(shape.getTranslateX() + deltaX);
            shape.setTranslateY(shape.getTranslateY() + deltaY);
        }
    }

    public void clearCanvas() {
        contentGroup.getChildren().clear();
    }

    public Group getContentGroup() {
        return contentGroup;
    }

    public void markAsModified() {
        isModified = true;
    }

    public void resetModificationStatus() {
        isModified = false;
    }
    public static boolean isModified() {
        return isModified;
    }

    public void setStrokeWidth(double strokeWidth) {
        this.currentStrokeWidth = strokeWidth;
    }
}
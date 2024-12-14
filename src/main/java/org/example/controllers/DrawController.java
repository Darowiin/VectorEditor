package org.example.controllers;

import javafx.scene.Cursor;
import org.example.enums.ToolMode;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public class DrawController {
    @FXML Pane drawingArea;
    private Group contentGroup;
    private ToolController toolController;
    private ColorController colorController;

    private Rectangle currentRectangle;
    private Circle currentCircle;
    private Line currentLine;

    private double startX, startY;
    private static boolean isModified = false;

    public void initialize(Pane drawingArea, ToolController toolController, ColorController colorController) {
        this.drawingArea = drawingArea;
        this.toolController = toolController;
        this.colorController = colorController;

        // Создаём Group и добавляем его в drawingArea
        contentGroup = new Group();
        drawingArea.getChildren().add(contentGroup);

        // Привязка событий мыши
        drawingArea.setOnMousePressed(this::handleMousePressed);
        drawingArea.setOnMouseDragged(this::handleMouseDragged);
        drawingArea.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        double localX = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
        double localY = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

        startX = localX;
        startY = localY;

        ToolMode currentTool = toolController.getCurrentTool();
        Color currentColor = colorController.getCurrentColor();

        if (currentTool == ToolMode.RECTANGLE) {
            currentRectangle = new Rectangle(startX, startY, 0, 0);
            currentRectangle.setFill(Color.TRANSPARENT);
            currentRectangle.setStroke(currentColor);
            currentRectangle.setStrokeWidth(2);
            contentGroup.getChildren().add(currentRectangle);
            markAsModified();
        } else if (currentTool == ToolMode.CIRCLE) {
            currentCircle = new Circle(startX, startY, 0);
            currentCircle.setFill(Color.TRANSPARENT);
            currentCircle.setStroke(currentColor);
            currentCircle.setStrokeWidth(2);
            contentGroup.getChildren().add(currentCircle);
            markAsModified();
        } else if (currentTool == ToolMode.LINE) {
            currentLine = new Line(startX, startY, startX, startY);
            currentLine.setStroke(currentColor);
            currentLine.setStrokeWidth(2);
            contentGroup.getChildren().add(currentLine);
            markAsModified();
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        double localX = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getX();
        double localY = contentGroup.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();

        if (currentRectangle != null) {
            double width = Math.abs(localX - startX);
            double height = Math.abs(localY - startY);
            currentRectangle.setWidth(width);
            currentRectangle.setHeight(height);
            if (localX < startX) currentRectangle.setX(localX);
            if (localY < startY) currentRectangle.setY(localY);
            markAsModified();
        } else if (currentCircle != null) {
            double radius = Math.sqrt(Math.pow(localX - startX, 2) + Math.pow(localY - startY, 2));
            currentCircle.setRadius(radius);
            markAsModified();
        } else if (currentLine != null) {
            currentLine.setEndX(localX);
            currentLine.setEndY(localY);
            markAsModified();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (currentRectangle != null) {
            enableResizing(currentRectangle);
            currentRectangle = null;
        } else if (currentCircle != null) {
            enableResizing(currentCircle);
            currentCircle = null;
        } else if (currentLine != null) {
            enableResizing(currentLine);
            currentLine = null;
        }
        markAsModified();
    }

    protected void enableResizing(javafx.scene.shape.Shape shape) {
        if (shape instanceof Line) {
            enableLineResizing((Line) shape);
        } else if (shape instanceof Circle) {
            enableCircleResizing((Circle) shape);
        } else if (shape instanceof Rectangle) {
            enableRectangleResizing((Rectangle) shape);
        }
    }

    private void enableLineResizing(Line line) {
        javafx.scene.shape.Circle startHandle = createHandle(line.getStartX(), line.getStartY());
        javafx.scene.shape.Circle endHandle = createHandle(line.getEndX(), line.getEndY());

        toggleHandlesVisibility(false, startHandle, endHandle);

        line.setOnMouseClicked(e -> {
            boolean visible = !startHandle.isVisible();
            toggleHandlesVisibility(visible, startHandle, endHandle);
        });

        startHandle.setOnMouseDragged(e -> {
            line.setStartX(e.getX());
            line.setStartY(e.getY());
            startHandle.setCenterX(e.getX());
            startHandle.setCenterY(e.getY());
        });

        endHandle.setOnMouseDragged(e -> {
            line.setEndX(e.getX());
            line.setEndY(e.getY());
            endHandle.setCenterX(e.getX());
            endHandle.setCenterY(e.getY());
        });

        contentGroup.getChildren().addAll(startHandle, endHandle);
    }

    private void enableCircleResizing(Circle circle) {
        javafx.scene.shape.Circle radiusHandle = createHandle(circle.getCenterX() + circle.getRadius(), circle.getCenterY());


        toggleHandlesVisibility(false, radiusHandle);

        circle.setOnMouseClicked(e -> {
            boolean visible = !radiusHandle.isVisible();
            toggleHandlesVisibility(visible, radiusHandle);
        });

        radiusHandle.setOnMouseDragged(e -> {
            double newRadius = Math.sqrt(Math.pow(e.getX() - circle.getCenterX(), 2) + Math.pow(e.getY() - circle.getCenterY(), 2));
            circle.setRadius(newRadius);
            radiusHandle.setCenterX(circle.getCenterX() + circle.getRadius());
        });

        contentGroup.getChildren().add(radiusHandle);
    }

    private void enableRectangleResizing(Rectangle rectangle) {
        // Создаем "ручки" для углов
        javafx.scene.shape.Circle topLeftHandle = createHandle(rectangle.getX(), rectangle.getY());
        javafx.scene.shape.Circle topRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
        javafx.scene.shape.Circle bottomLeftHandle = createHandle(rectangle.getX(), rectangle.getY() + rectangle.getHeight());
        javafx.scene.shape.Circle bottomRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());

        toggleHandlesVisibility(false, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);

        rectangle.setOnMouseClicked(e -> {
            boolean visible = !topLeftHandle.isVisible(); // Переключение видимости
            toggleHandlesVisibility(visible, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        topLeftHandle.setOnMouseDragged(e -> {
            double newWidth = rectangle.getX() + rectangle.getWidth() - e.getX();
            double newHeight = rectangle.getY() + rectangle.getHeight() - e.getY();

            if (newWidth > 0 && newHeight > 0) {
                rectangle.setX(e.getX());
                rectangle.setY(e.getY());
                rectangle.setWidth(newWidth);
                rectangle.setHeight(newHeight);
            } else if (newWidth <= 0) {
                rectangle.setX(rectangle.getX() + rectangle.getWidth());
                rectangle.setWidth(-newWidth);
            } else if (newHeight <= 0) {
                rectangle.setY(rectangle.getY() + rectangle.getHeight());
                rectangle.setHeight(-newHeight);
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        topRightHandle.setOnMouseDragged(e -> {
            double newWidth = e.getX() - rectangle.getX();
            double newHeight = rectangle.getY() + rectangle.getHeight() - e.getY();

            if (newWidth > 0 && newHeight > 0) {
                rectangle.setY(e.getY());
                rectangle.setWidth(newWidth);
                rectangle.setHeight(newHeight);
            } else if (newWidth <= 0) {
                rectangle.setX(e.getX());
                rectangle.setWidth(-newWidth);
            } else if (newHeight <= 0) {
                rectangle.setY(rectangle.getY() + rectangle.getHeight());
                rectangle.setHeight(-newHeight);
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        bottomLeftHandle.setOnMouseDragged(e -> {
            double newWidth = rectangle.getX() + rectangle.getWidth() - e.getX();
            double newHeight = e.getY() - rectangle.getY();

            if (newWidth > 0 && newHeight > 0) {
                rectangle.setX(e.getX());
                rectangle.setWidth(newWidth);
                rectangle.setHeight(newHeight);
            } else if (newWidth <= 0) {
                rectangle.setX(rectangle.getX() + rectangle.getWidth());
                rectangle.setWidth(-newWidth);
            } else if (newHeight <= 0) {
                rectangle.setY(e.getY());
                rectangle.setHeight(-newHeight);
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        bottomRightHandle.setOnMouseDragged(e -> {
            double newWidth = e.getX() - rectangle.getX();
            double newHeight = e.getY() - rectangle.getY();

            if (newWidth > 0 && newHeight > 0) {
                rectangle.setWidth(newWidth);
                rectangle.setHeight(newHeight);
            } else if (newWidth <= 0) {
                rectangle.setX(e.getX());
                rectangle.setWidth(-newWidth);
            } else if (newHeight <= 0) {
                rectangle.setY(e.getY());
                rectangle.setHeight(-newHeight);
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        contentGroup.getChildren().addAll(topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
    }

    private javafx.scene.shape.Circle createHandle(double x, double y) {
        javafx.scene.shape.Circle handle = new javafx.scene.shape.Circle(x, y, 5);
        handle.setFill(Color.RED);
        handle.setStroke(Color.BLACK);
        handle.setStrokeWidth(1);
        handle.setCursor(Cursor.HAND);
        handle.setId("draggable-handle");
        return handle;
    }

    private void updateRectangleHandles(Rectangle rectangle, javafx.scene.shape.Circle topLeftHandle,
                                        javafx.scene.shape.Circle topRightHandle,
                                        javafx.scene.shape.Circle bottomLeftHandle,
                                        javafx.scene.shape.Circle bottomRightHandle) {
        topLeftHandle.setCenterX(rectangle.getX());
        topLeftHandle.setCenterY(rectangle.getY());

        topRightHandle.setCenterX(rectangle.getX() + rectangle.getWidth());
        topRightHandle.setCenterY(rectangle.getY());

        bottomLeftHandle.setCenterX(rectangle.getX());
        bottomLeftHandle.setCenterY(rectangle.getY() + rectangle.getHeight());

        bottomRightHandle.setCenterX(rectangle.getX() + rectangle.getWidth());
        bottomRightHandle.setCenterY(rectangle.getY() + rectangle.getHeight());
    }

    private void toggleHandlesVisibility(boolean visible, javafx.scene.shape.Circle... handles) {
        for (javafx.scene.shape.Circle handle : handles) {
            handle.setVisible(visible);
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
    // Метод для сброса флага изменения
    public void resetModificationStatus() {
        isModified = false;
    }
    public static boolean isModified() {
        return isModified;
    }
}

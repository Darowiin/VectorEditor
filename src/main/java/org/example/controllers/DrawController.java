package org.example.controllers;

import javafx.scene.Cursor;
import javafx.scene.shape.*;
import org.example.enums.ToolMode;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;


public class DrawController {
    @FXML Pane drawingArea;
    private Group contentGroup;
    private ToolController toolController;
    private ColorController colorController;

    private Rectangle currentRectangle;
    private Ellipse currentEllipse;
    private Line currentLine;
    private Path currentCurve;

    private double startX, startY;
    private static boolean isModified = false;
    private double currentStrokeWidth = 2.0;

    public void initialize(Pane drawingArea, ToolController toolController, ColorController colorController) {
        this.drawingArea = drawingArea;
        this.toolController = toolController;
        this.colorController = colorController;

        contentGroup = new Group();
        drawingArea.getChildren().add(contentGroup);

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
        Color currentColorFill = colorController.getFillColor();

        if (currentTool == ToolMode.RECTANGLE) {
            currentRectangle = new Rectangle(startX, startY, 0, 0);
            currentRectangle.setFill(currentColorFill);
            currentRectangle.setStroke(currentColor);
            currentRectangle.setStrokeWidth(currentStrokeWidth);
            contentGroup.getChildren().add(currentRectangle);
            markAsModified();
        } else if (currentTool == ToolMode.ELLIPSE) {
            currentEllipse = new Ellipse(startX, startY, 0, 0);
            currentEllipse.setFill(currentColorFill);
            currentEllipse.setStroke(currentColor);
            currentEllipse.setStrokeWidth(currentStrokeWidth);
            contentGroup.getChildren().add(currentEllipse);
            markAsModified();
        } else if (currentTool == ToolMode.LINE) {
            currentLine = new Line(startX, startY, startX, startY);
            currentLine.setStroke(currentColor);
            currentLine.setStrokeWidth(currentStrokeWidth);
            contentGroup.getChildren().add(currentLine);
            markAsModified();
        }
        if (currentTool == ToolMode.CURVE) {
            currentCurve = new Path();
            currentCurve.setStroke(currentColor);
            currentCurve.setStrokeWidth(currentStrokeWidth);
            currentCurve.setFill(currentColorFill);

            MoveTo moveTo = new MoveTo(startX, startY);
            currentCurve.getElements().add(moveTo);

            contentGroup.getChildren().add(currentCurve);
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
        } else if (currentEllipse != null) {
            double width = Math.abs(localX - startX);
            double height = Math.abs(localY - startY);

            currentEllipse.setRadiusX(width);
            currentEllipse.setRadiusY(height);

            if (localX < startX) currentEllipse.setCenterX(localX);
            if (localY < startY) currentEllipse.setCenterY(localY);

            markAsModified();
        } else if (currentLine != null) {
            currentLine.setEndX(localX);
            currentLine.setEndY(localY);
            markAsModified();
        } else if (currentCurve != null) {
            PathElement lastElement = currentCurve.getElements().get(currentCurve.getElements().size() - 1);

            if (lastElement instanceof QuadCurveTo) {
                QuadCurveTo quad = (QuadCurveTo) lastElement;
                quad.setControlX(localX);
                quad.setControlY(localY);
            } else {
                QuadCurveTo quadCurveTo = new QuadCurveTo(startX, startY, localX, localY);
                currentCurve.getElements().add(quadCurveTo);
            }
            markAsModified();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (currentRectangle != null) {
            enableResizing(currentRectangle);
            currentRectangle = null;
        } else if (currentEllipse != null) {
            enableResizing(currentEllipse);
            currentEllipse = null;
        } else if (currentLine != null) {
            enableResizing(currentLine);
            currentLine = null;
        } else if (currentCurve != null) {
            enableResizing(currentCurve);
            currentCurve = null;
        }
        markAsModified();
    }

    protected void enableResizing(javafx.scene.shape.Shape shape) {
        if (shape instanceof Line) {
            enableLineResizing((Line) shape);
        } else if (shape instanceof Ellipse) {
            enableEllipseResizing((Ellipse) shape);
        } else if (shape instanceof Rectangle) {
            enableRectangleResizing((Rectangle) shape);
        } else if (shape instanceof Path) {
            enableCurveResizing((Path) shape);
        }
    }

    private void enableCurveResizing(Path shape) {
        for (PathElement element : shape.getElements()) {
            if (element instanceof MoveTo moveTo) {
                javafx.scene.shape.Circle startHandle = createHandle(moveTo.getX(), moveTo.getY());

                toggleHandlesVisibility(true, startHandle);

                startHandle.setOnMouseDragged(e -> {
                    moveTo.setX(e.getX());
                    moveTo.setY(e.getY());
                    startHandle.setCenterX(e.getX());
                    startHandle.setCenterY(e.getY());
                    System.out.println("StartHandle moved");
                });

                shape.setOnMouseClicked(e -> {
                    boolean visible = !startHandle.isVisible();
                    toggleHandlesVisibility(visible, startHandle);
                });


                contentGroup.getChildren().add(startHandle);

            } else if (element instanceof QuadCurveTo quadCurveTo) {
                javafx.scene.shape.Circle controlHandle = createHandle(quadCurveTo.getControlX(), quadCurveTo.getControlY());
                javafx.scene.shape.Circle endHandle = createHandle(quadCurveTo.getX(), quadCurveTo.getY());

                toggleHandlesVisibility(false, controlHandle, endHandle);

                controlHandle.setOnMouseDragged(e -> {
                    quadCurveTo.setControlX(e.getX());
                    quadCurveTo.setControlY(e.getY());
                    controlHandle.setCenterX(e.getX());
                    controlHandle.setCenterY(e.getY());
                });

                endHandle.setOnMouseDragged(e -> {
                    quadCurveTo.setX(e.getX());
                    quadCurveTo.setY(e.getY());
                    endHandle.setCenterX(e.getX());
                    endHandle.setCenterY(e.getY());
                });

                shape.setOnMouseClicked(e -> {
                    boolean visible = !controlHandle.isVisible();
                    toggleHandlesVisibility(visible, controlHandle, endHandle);
                });

                contentGroup.getChildren().addAll(controlHandle, endHandle);
            } else if (element instanceof LineTo lineTo) {
                javafx.scene.shape.Circle endHandle = createHandle(lineTo.getX(), lineTo.getY());

                toggleHandlesVisibility(false, endHandle);

                endHandle.setOnMouseDragged(e -> {
                    lineTo.setX(e.getX());
                    lineTo.setY(e.getY());
                    endHandle.setCenterX(e.getX());
                    endHandle.setCenterY(e.getY());
                });

                shape.setOnMouseClicked(e -> {
                    boolean visible = !endHandle.isVisible();
                    toggleHandlesVisibility(visible, endHandle);
                });

                contentGroup.getChildren().add(endHandle);
            }
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

    private void enableEllipseResizing(Ellipse ellipse) {
        javafx.scene.shape.Circle widthHandle = createHandle(ellipse.getCenterX() + ellipse.getRadiusX(), ellipse.getCenterY()); // Для ширины
        javafx.scene.shape.Circle heightHandle = createHandle(ellipse.getCenterX(), ellipse.getCenterY() + ellipse.getRadiusY()); // Для высоты

        toggleHandlesVisibility(false, widthHandle, heightHandle);

        ellipse.setOnMouseClicked(e -> {
            boolean visible = !widthHandle.isVisible();
            toggleHandlesVisibility(visible, widthHandle, heightHandle);
        });

        widthHandle.setOnMouseDragged(e -> {
            double newRadiusX = Math.abs(e.getX() - ellipse.getCenterX());
            ellipse.setRadiusX(newRadiusX);
            widthHandle.setCenterX(ellipse.getCenterX() + newRadiusX);
        });

        heightHandle.setOnMouseDragged(e -> {
            double newRadiusY = Math.abs(e.getY() - ellipse.getCenterY());
            ellipse.setRadiusY(newRadiusY);
            heightHandle.setCenterY(ellipse.getCenterY() + newRadiusY);
        });
        contentGroup.getChildren().addAll(widthHandle, heightHandle);
    }

    private void enableRectangleResizing(Rectangle rectangle) {
        javafx.scene.shape.Circle topLeftHandle = createHandle(rectangle.getX(), rectangle.getY());
        javafx.scene.shape.Circle topRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
        javafx.scene.shape.Circle bottomLeftHandle = createHandle(rectangle.getX(), rectangle.getY() + rectangle.getHeight());
        javafx.scene.shape.Circle bottomRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());

        toggleHandlesVisibility(false, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);

        rectangle.setOnMouseClicked(e -> {
            boolean visible = !topLeftHandle.isVisible();
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
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        bottomRightHandle.setOnMouseDragged(e -> {
            double newWidth = e.getX() - rectangle.getX();
            double newHeight = e.getY() - rectangle.getY();

            if (newWidth > 0 && newHeight > 0) {
                rectangle.setWidth(newWidth);
                rectangle.setHeight(newHeight);
            }

            updateRectangleHandles(rectangle, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
        });

        contentGroup.getChildren().addAll(topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
    }

    private javafx.scene.shape.Circle createHandle(double x, double y) {
        javafx.scene.shape.Circle handle = new javafx.scene.shape.Circle(x, y, 1.5*currentStrokeWidth);
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

package org.example.controllers;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.example.enums.ToolMode;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.shape.*;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DrawController {
    @FXML Pane drawingArea;
    private Group contentGroup;
    private ToolController toolController;
    private ColorController colorController;
    private HistoryController historyController;

    private Rectangle selectionRectangle;
    private Rectangle currentRectangle;
    private Ellipse currentEllipse;
    private Line currentLine;
    private Path currentCurve;

    private double startX, startY;
    private static boolean isModified = false;
    private boolean isDraggingSelectedShapes = false; // Флаг для перетаскивания
    private boolean isSelecting = false;             // Флаг для выделения области
    private boolean isDrawing = false;               // Флаг для рисования
    private double selectionStartX, selectionStartY; // Координаты начала выделения
    private double currentStrokeWidth = 2.0;
    private List<Shape> selectedShapes = new ArrayList<>();
    protected final Map<Shape, List<Circle>> shapeHandlesMap = new HashMap<>();

    public void initialize(Pane drawingArea, ToolController toolController, ColorController colorController, HistoryController historyController) {
        this.drawingArea = drawingArea;
        this.toolController = toolController;
        this.colorController = colorController;
        this.historyController = historyController;

        this.contentGroup = new Group();
        drawingArea.getChildren().add(contentGroup);

        activateDrawingHandlers();
    }

    public void addShape(Shape shape) {
        if (!contentGroup.getChildren().contains(shape)) {
            contentGroup.getChildren().add(shape);
            historyController.addAction(
                    () -> {
                        contentGroup.getChildren().remove(shape);
                        removeHandlesFromShape(shape);
                    },
                    () -> {
                        contentGroup.getChildren().add(shape);
                        enableResizing(shape);
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
            } else if (currentTool == ToolMode.RECTANGLE || currentTool == ToolMode.ELLIPSE || currentTool == ToolMode.LINE || currentTool == ToolMode.CURVE) {
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
                }
            }
        });

        drawingArea.setOnMouseReleased(event -> {
            if (isSelecting && selectionRectangle != null) {
                Bounds selectionBounds = selectionRectangle.getBoundsInParent();
                selectedShapes.clear();
                for (javafx.scene.Node node : contentGroup.getChildren()) {
                    if (node instanceof Shape shape && shape.getBoundsInParent().intersects(selectionBounds)) {
                        selectedShapes.add(shape);
                    }
                }
                contentGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
                isSelecting = false;
            } else if (isDraggingSelectedShapes) {
                isDraggingSelectedShapes = false;
                drawingArea.setCursor(Cursor.DEFAULT);
                for (int i = 0; i < selectedShapes.size(); i++) {
                    enableResizing(selectedShapes.get(i));
                }
                finalPositions.clear();
                for (Shape shape : selectedShapes) {
                    finalPositions.put(shape, new Point2D(shape.getTranslateX(), shape.getTranslateY()));
                }

                historyController.addAction(
                        () -> {
                            for (Shape shape : initialPositions.keySet()) {
                                Point2D position = initialPositions.get(shape);
                                System.out.println("Undo - Setting position: " + position);
                                shape.setTranslateX(position.getX());
                                shape.setTranslateY(position.getY());
                                removeHandlesFromShape(shape);
                            }
                        },
                        () -> {
                            for (Shape shape : finalPositions.keySet()) {
                                Point2D position = finalPositions.get(shape);
                                System.out.println("Redo - Setting position: " + position);
                                shape.setTranslateX(position.getX());
                                shape.setTranslateY(position.getY());
                                enableResizing(shape);
                            }
                        }
                );
                selectedShapes.clear();
                markAsModified();
            } else if (isDrawing) {
                isDrawing = false;
                if (currentRectangle != null) {
                    enableResizing(currentRectangle);
                    currentRectangle = null;
                } else if (currentEllipse != null) {
                    enableResizing(currentEllipse);
                    currentEllipse = null;
                } else if (currentCurve != null) {
                    enableResizing(currentCurve);
                    currentCurve = null;
                } else if (currentLine != null) {
                    enableResizing(currentLine);
                    currentLine = null;
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

    protected void enableResizing(Shape shape) {
        List<Circle> handles = new ArrayList<>();

        if (shape instanceof Line) {
            enableLineResizing((Line) shape, handles);
        } else if (shape instanceof Ellipse) {
            enableEllipseResizing((Ellipse) shape, handles);
        } else if (shape instanceof Rectangle) {
            enableRectangleResizing((Rectangle) shape, handles);
        } else if (shape instanceof Path) {
            enableCurveResizing((Path) shape, handles);
        }

        shapeHandlesMap.put(shape, handles);
        contentGroup.getChildren().addAll(handles);
    }

    private void removeHandlesFromShape(Shape shape) {
        List<Circle> handles = shapeHandlesMap.get(shape);
        if (handles != null) {
            contentGroup.getChildren().removeAll(handles);
            shapeHandlesMap.remove(shape);
        }
    }

    private void enableCurveResizing(Path shape, List<Circle> handles) {
        for (PathElement element : shape.getElements()) {
            if (element instanceof MoveTo moveTo) {
                javafx.scene.shape.Circle startHandle = createHandle(moveTo.getX(), moveTo.getY());

                toggleHandlesVisibility(true, startHandle);

                startHandle.setOnMouseDragged(e -> {
                    moveTo.setX(e.getX());
                    moveTo.setY(e.getY());
                    startHandle.setCenterX(e.getX());
                    startHandle.setCenterY(e.getY());
                });

                shape.setOnMouseClicked(e -> {
                    if (toolController.getCurrentTool() == ToolMode.SELECT) {
                        boolean visible = !startHandle.isVisible();
                        toggleHandlesVisibility(visible, startHandle);
                    }
                });


                handles.add(startHandle);

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
                    if (toolController.getCurrentTool() == ToolMode.SELECT) {
                        boolean visible = !controlHandle.isVisible();
                        toggleHandlesVisibility(visible, controlHandle, endHandle);
                    }
                });

                handles.add(controlHandle);
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
                    if (toolController.getCurrentTool() == ToolMode.SELECT) {
                        boolean visible = !endHandle.isVisible();
                        toggleHandlesVisibility(visible, endHandle);
                    }
                });

                handles.add(endHandle);
            }
        }
    }

    private void enableLineResizing(Line line, List<Circle> handles) {
        Circle startHandle = createHandle(line.getStartX(), line.getStartY());
        Circle endHandle = createHandle(line.getEndX(), line.getEndY());

        toggleHandlesVisibility(false, startHandle, endHandle);

        line.setOnMouseClicked(e -> {
            if (toolController.getCurrentTool() == ToolMode.SELECT) {
                boolean visible = !startHandle.isVisible();
                toggleHandlesVisibility(visible, startHandle, endHandle);
            }
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

        handles.add(startHandle);
        handles.add(endHandle);
    }

    private void enableEllipseResizing(Ellipse ellipse, List<Circle> handles) {
        Circle widthHandle = createHandle(ellipse.getCenterX() + ellipse.getRadiusX(), ellipse.getCenterY());
        Circle heightHandle = createHandle(ellipse.getCenterX(), ellipse.getCenterY() + ellipse.getRadiusY());

        toggleHandlesVisibility(false, widthHandle, heightHandle);

        ellipse.setOnMouseClicked(e -> {
            if (toolController.getCurrentTool() == ToolMode.SELECT) {
                boolean visible = !widthHandle.isVisible();
                toggleHandlesVisibility(visible, widthHandle, heightHandle);
            }
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

        handles.add(widthHandle);
        handles.add(heightHandle);
    }

    private void enableRectangleResizing(Rectangle rectangle, List<Circle> handles) {
        javafx.scene.shape.Circle topLeftHandle = createHandle(rectangle.getX(), rectangle.getY());
        javafx.scene.shape.Circle topRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
        javafx.scene.shape.Circle bottomLeftHandle = createHandle(rectangle.getX(), rectangle.getY() + rectangle.getHeight());
        javafx.scene.shape.Circle bottomRightHandle = createHandle(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());

        toggleHandlesVisibility(false, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);

        rectangle.setOnMouseClicked(e -> {
            if (toolController.getCurrentTool() == ToolMode.SELECT) {
                boolean visible = !topLeftHandle.isVisible();
                toggleHandlesVisibility(visible, topLeftHandle, topRightHandle, bottomLeftHandle, bottomRightHandle);
            }
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

        handles.add(topLeftHandle);
        handles.add(topRightHandle);
        handles.add(bottomLeftHandle);
        handles.add(bottomRightHandle);
    }

    private Circle createHandle(double x, double y) {
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
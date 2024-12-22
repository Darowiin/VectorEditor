package org.example.controllers;

import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.example.enums.ToolMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResizingController {
    ToolController toolController;
    DrawController drawController;

    protected final Map<Shape, List<Circle>> shapeHandlesMap = new HashMap<>();

    public void initialize(ToolController toolController, DrawController drawController) {
        this.toolController = toolController;
        this.drawController = drawController;
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
        } else if (shape instanceof Polygon) {
            enablePolygonResizing((Polygon) shape, handles);
        }

        shapeHandlesMap.put(shape, handles);
        shape.setOnMouseClicked(e -> {
            if (toolController.getCurrentTool() == ToolMode.SELECT) {
                List<Circle> circles = shapeHandlesMap.get(shape);
                if (circles != null) {
                    boolean visible = !circles.get(0).isVisible();
                    toggleHandlesVisibility(visible, circles.toArray(new Circle[0]));
                }
            }
        });
        drawController.contentGroup.getChildren().addAll(handles);
    }

    private void enablePolygonResizing(Polygon shape, List<Circle> handles) {
        ObservableList<Double> points = shape.getPoints();

        for (int i = 0; i < points.size(); i += 2) {
            double x = points.get(i);
            double y = points.get(i + 1);

            Circle handle = createHandle(x, y);

            int index = i;
            handle.setOnMouseDragged(e -> {
                points.set(index, e.getX());
                points.set(index + 1, e.getY());
                handle.setCenterX(e.getX());
                handle.setCenterY(e.getY());
            });

            toggleHandlesVisibility(false, handle);

            handles.add(handle);
        }
    }

    private void enableCurveResizing(Path shape, List<Circle> handles) {
        for (PathElement element : shape.getElements()) {
            if (element instanceof MoveTo moveTo) {
                Circle startHandle = createHandle(moveTo.getX(), moveTo.getY());

                toggleHandlesVisibility(true, startHandle);

                startHandle.setOnMouseDragged(e -> {
                    moveTo.setX(e.getX());
                    moveTo.setY(e.getY());
                    startHandle.setCenterX(e.getX());
                    startHandle.setCenterY(e.getY());
                });

                handles.add(startHandle);

            } else if (element instanceof QuadCurveTo quadCurveTo) {
                Circle controlHandle = createHandle(quadCurveTo.getControlX(), quadCurveTo.getControlY());
                Circle endHandle = createHandle(quadCurveTo.getX(), quadCurveTo.getY());

                toggleHandlesVisibility(true, controlHandle, endHandle);

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

                handles.add(controlHandle);
            } else if (element instanceof LineTo lineTo) {
                Circle endHandle = createHandle(lineTo.getX(), lineTo.getY());

                toggleHandlesVisibility(true, endHandle);

                endHandle.setOnMouseDragged(e -> {
                    lineTo.setX(e.getX());
                    lineTo.setY(e.getY());
                    endHandle.setCenterX(e.getX());
                    endHandle.setCenterY(e.getY());
                });

                handles.add(endHandle);
            }
        }
    }

    private void enableLineResizing(Line line, List<Circle> handles) {
        Circle startHandle = createHandle(line.getStartX(), line.getStartY());
        Circle endHandle = createHandle(line.getEndX(), line.getEndY());

        toggleHandlesVisibility(false, startHandle, endHandle);

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
        javafx.scene.shape.Circle handle = new javafx.scene.shape.Circle(x, y, 1.5*drawController.currentStrokeWidth);
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

    protected void toggleHandlesVisibility(boolean visible, javafx.scene.shape.Circle... handles) {
        for (javafx.scene.shape.Circle handle : handles) {
            handle.setVisible(visible);
        }
    }

    protected void removeHandlesFromShape(Shape shape) {
        List<Circle> handles = shapeHandlesMap.get(shape);
        if (handles != null) {
            drawController.contentGroup.getChildren().removeAll(handles);
            shapeHandlesMap.remove(shape);
        }
    }
}

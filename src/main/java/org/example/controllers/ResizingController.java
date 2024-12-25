package org.example.controllers;

import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import org.example.enums.ToolMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResizingController {
    ToolController toolController;
    DrawController drawController;
    HistoryController historyController;

    protected final Map<Shape, List<Circle>> shapeHandlesMap = new HashMap<>();

    public void initialize(ToolController toolController, DrawController drawController, HistoryController historyController) {
        this.toolController = toolController;
        this.drawController = drawController;
        this.historyController = historyController;
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

                toggleHandlesVisibility(false, startHandle);

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

                handles.add(controlHandle);
            } else if (element instanceof LineTo lineTo) {
                Circle endHandle = createHandle(lineTo.getX(), lineTo.getY());

                toggleHandlesVisibility(false, endHandle);

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

    protected void toggleHandlesVisibility(boolean visible, Circle... handles) {
        for (Circle handle : handles) {
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

    protected void updateShapePosition(Shape shape) {
        double deltaX = shape.getTranslateX();
        double deltaY = shape.getTranslateY();

        double[] oldCoordinates = getShapeCoordinates(shape);

        if (shape instanceof Rectangle rect) {
            rect.setX(rect.getX() + deltaX);
            rect.setY(rect.getY() + deltaY);
        } else if (shape instanceof Ellipse ellipse) {
            ellipse.setCenterX(ellipse.getCenterX() + deltaX);
            ellipse.setCenterY(ellipse.getCenterY() + deltaY);
        } else if (shape instanceof Line line) {
            line.setStartX(line.getStartX() + deltaX);
            line.setStartY(line.getStartY() + deltaY);
            line.setEndX(line.getEndX() + deltaX);
            line.setEndY(line.getEndY() + deltaY);
        } else if (shape instanceof Polygon polygon) {
            ObservableList<Double> points = polygon.getPoints();
            for (int i = 0; i < points.size(); i += 2) {
                points.set(i, points.get(i) + deltaX);       // X
                points.set(i + 1, points.get(i + 1) + deltaY); // Y
            }
        } else if (shape instanceof Path path) {
            for (PathElement element : path.getElements()) {
                if (element instanceof MoveTo moveTo) {
                    moveTo.setX(moveTo.getX() + deltaX);
                    moveTo.setY(moveTo.getY() + deltaY);
                } else if (element instanceof LineTo lineTo) {
                    lineTo.setX(lineTo.getX() + deltaX);
                    lineTo.setY(lineTo.getY() + deltaY);
                } else if (element instanceof QuadCurveTo quadCurveTo) {
                    quadCurveTo.setControlX(quadCurveTo.getControlX() + deltaX);
                    quadCurveTo.setControlY(quadCurveTo.getControlY() + deltaY);
                    quadCurveTo.setX(quadCurveTo.getX() + deltaX);
                    quadCurveTo.setY(quadCurveTo.getY() + deltaY);
                }
            }
        } else if (shape instanceof Text text) {
            text.setX(text.getX() + deltaX);
            text.setY(text.getY() + deltaY);
        }

        shape.setTranslateX(0);
        shape.setTranslateY(0);

        double[] newCoordinates = getShapeCoordinates(shape);
        historyController.addAction(
                () -> {
                    setShapeCoordinates(shape, oldCoordinates);
                    removeHandlesFromShape(shape);
                    enableResizing(shape);
                },
                () -> {
                    setShapeCoordinates(shape, newCoordinates);
                    removeHandlesFromShape(shape);
                    enableResizing(shape);
                }
        );
    }

    private double[] getShapeCoordinates(Shape shape) {
        if (shape instanceof Rectangle rect) {
            return new double[]{rect.getX(), rect.getY()};
        } else if (shape instanceof Ellipse ellipse) {
            return new double[]{ellipse.getCenterX(), ellipse.getCenterY()};
        } else if (shape instanceof Line line) {
            return new double[]{line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()};
        } else if (shape instanceof Polygon polygon) {
            double[] points = new double[polygon.getPoints().size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = polygon.getPoints().get(i);
            }
            return points;
        } else if (shape instanceof Path path) {
            List<Double> coords = new ArrayList<>();
            for (PathElement element : path.getElements()) {
                if (element instanceof MoveTo moveTo) {
                    coords.add(moveTo.getX());
                    coords.add(moveTo.getY());
                } else if (element instanceof LineTo lineTo) {
                    coords.add(lineTo.getX());
                    coords.add(lineTo.getY());
                } else if (element instanceof QuadCurveTo quadCurveTo) {
                    coords.add(quadCurveTo.getControlX());
                    coords.add(quadCurveTo.getControlY());
                    coords.add(quadCurveTo.getX());
                    coords.add(quadCurveTo.getY());
                }
            }
            return coords.stream().mapToDouble(Double::doubleValue).toArray();
        }
        return new double[0];
    }

    private void setShapeCoordinates(Shape shape, double[] coords) {
        if (shape instanceof Rectangle rect) {
            rect.setX(coords[0]);
            rect.setY(coords[1]);
        } else if (shape instanceof Ellipse ellipse) {
            ellipse.setCenterX(coords[0]);
            ellipse.setCenterY(coords[1]);
        } else if (shape instanceof Line line) {
            line.setStartX(coords[0]);
            line.setStartY(coords[1]);
            line.setEndX(coords[2]);
            line.setEndY(coords[3]);
        } else if (shape instanceof Polygon polygon) {
            ObservableList<Double> points = polygon.getPoints();
            points.clear();
            for (double coord : coords) {
                points.add(coord);
            }
        } else if (shape instanceof Path path) {
            int index = 0;
            for (PathElement element : path.getElements()) {
                if (element instanceof MoveTo moveTo) {
                    moveTo.setX(coords[index++]);
                    moveTo.setY(coords[index++]);
                } else if (element instanceof LineTo lineTo) {
                    lineTo.setX(coords[index++]);
                    lineTo.setY(coords[index++]);
                } else if (element instanceof QuadCurveTo quadCurveTo) {
                    quadCurveTo.setControlX(coords[index++]);
                    quadCurveTo.setControlY(coords[index++]);
                    quadCurveTo.setX(coords[index++]);
                    quadCurveTo.setY(coords[index++]);
                }
            }
        }
    }
}
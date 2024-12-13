package org.example.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import org.example.enums.ToolMode;
import org.example.models.ShapeData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DrawController {
    @FXML Pane drawingArea;
    private Group contentGroup; // Контейнер для фигур
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
            currentRectangle = null;
        } else if (currentCircle != null) {
            currentCircle = null;
        } else if (currentLine != null) {
            currentLine = null;
        }
        markAsModified();
    }

    public void clearCanvas() {
        contentGroup.getChildren().clear();
    }

    public Group getContentGroup() {
        return contentGroup;
    }

    public void saveToJSON(File file) throws IOException {
        List<ShapeData> shapeDataList = new ArrayList<>();
        for (javafx.scene.Node node : contentGroup.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) node;
                ShapeData data = new ShapeData();
                data.setType("rectangle");
                data.setX(rectangle.getX());
                data.setY(rectangle.getY());
                data.setWidth(rectangle.getWidth());
                data.setHeight(rectangle.getHeight());
                data.setStrokeColor(rectangle.getStroke().toString());
                data.setStrokeWidth(rectangle.getStrokeWidth());
                shapeDataList.add(data);
            } else if (node instanceof Circle) {
                Circle circle = (Circle) node;
                ShapeData data = new ShapeData();
                data.setType("circle");
                data.setCenterX(circle.getCenterX());
                data.setCenterY(circle.getCenterY());
                data.setRadius(circle.getRadius());
                data.setStrokeColor(circle.getStroke().toString());
                data.setStrokeWidth(circle.getStrokeWidth());
                shapeDataList.add(data);
            } else if (node instanceof Line) {
                Line line = (Line) node;
                ShapeData data = new ShapeData();
                data.setType("line");
                data.setX(line.getStartX());
                data.setY(line.getStartY());
                data.setWidth(line.getEndX());
                data.setHeight(line.getEndY());
                data.setStrokeColor(line.getStroke().toString());
                data.setStrokeWidth(line.getStrokeWidth());
                shapeDataList.add(data);
            }
        }
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(shapeDataList, writer);
        }
    }

    public void loadFromJSON(File file) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ShapeData>>() {}.getType();
        try (Reader reader = new FileReader(file)) {
            List<ShapeData> shapeDataList = gson.fromJson(reader, listType);
            contentGroup.getChildren().clear();
            for (ShapeData data : shapeDataList) {
                switch (data.getType()) {
                    case "rectangle":
                        Rectangle rectangle = new Rectangle(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        rectangle.setStroke(Color.web(data.getStrokeColor()));
                        rectangle.setStrokeWidth(data.getStrokeWidth());
                        rectangle.setFill(Color.TRANSPARENT);
                        contentGroup.getChildren().add(rectangle);
                        break;
                    case "circle":
                        Circle circle = new Circle(data.getCenterX(), data.getCenterY(), data.getRadius());
                        circle.setStroke(Color.web(data.getStrokeColor()));
                        circle.setStrokeWidth(data.getStrokeWidth());
                        circle.setFill(Color.TRANSPARENT);
                        contentGroup.getChildren().add(circle);
                        break;
                    case "line":
                        Line line = new Line(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        line.setStroke(Color.web(data.getStrokeColor()));
                        line.setStrokeWidth(data.getStrokeWidth());
                        contentGroup.getChildren().add(line);
                        break;
                }
            }
        }
    }

    public void saveToSvg(File file) throws IOException {
        StringBuilder svgContent = new StringBuilder();

        // Начало SVG-файла
        svgContent.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        // Преобразование каждого элемента contentGroup в SVG
        for (javafx.scene.Node node : contentGroup.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                svgContent.append(String.format(Locale.US,
                        "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"none\" />\n",
                        rect.getX(),
                        rect.getY(),
                        rect.getWidth(),
                        rect.getHeight(),
                        toHexString((Color) rect.getStroke()),
                        rect.getStrokeWidth()
                ));
            } else if (node instanceof Circle) {
                Circle circle = (Circle) node;
                svgContent.append(String.format(Locale.US,
                        "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"none\" />\n",
                        circle.getCenterX(),
                        circle.getCenterY(),
                        circle.getRadius(),
                        toHexString((Color) circle.getStroke()),
                        circle.getStrokeWidth()
                ));
            } else if (node instanceof Line) {
                Line line = (Line) node;
                svgContent.append(String.format(Locale.US,
                        "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" />\n",
                        line.getStartX(),
                        line.getStartY(),
                        line.getEndX(),
                        line.getEndY(),
                        toHexString((Color) line.getStroke()),
                        line.getStrokeWidth()
                ));
            }
        }

        // Конец SVG-файла
        svgContent.append("</svg>");

        // Запись в файл
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(svgContent.toString());
        }
    }

    // Метод для преобразования Color в строку HEX
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    public void loadFromSvg(File file) throws IOException, ParseException {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        Document doc = factory.createDocument(file.toURI().toString());

        contentGroup.getChildren().clear();
        NodeList elements = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < elements.getLength(); i++) {
            processSvgNode(elements.item(i));
        }
    }
    private void processSvgNode(Node node) throws ParseException {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            switch (element.getTagName()) {
                case "rect":
                    Rectangle rect = new Rectangle(
                            parseDouble(element.getAttribute("x")),
                            parseDouble(element.getAttribute("y")),
                            parseDouble(element.getAttribute("width")),
                            parseDouble(element.getAttribute("height"))
                    );
                    rect.setStroke(Color.web(element.getAttribute("stroke")));
                    rect.setStrokeWidth(parseDouble(element.getAttribute("stroke-width")));
                    rect.setFill(Color.TRANSPARENT);
                    contentGroup.getChildren().add(rect);
                    break;

                case "circle":
                    Circle circle = new Circle(
                            parseDouble(element.getAttribute("cx")),
                            parseDouble(element.getAttribute("cy")),
                            parseDouble(element.getAttribute("r"))
                    );
                    circle.setStroke(Color.web(element.getAttribute("stroke")));
                    circle.setStrokeWidth(parseDouble(element.getAttribute("stroke-width")));
                    circle.setFill(Color.TRANSPARENT);
                    contentGroup.getChildren().add(circle);
                    break;

                case "line":
                    Line line = new Line(
                            parseDouble(element.getAttribute("x1")),
                            parseDouble(element.getAttribute("y1")),
                            parseDouble(element.getAttribute("x2")),
                            parseDouble(element.getAttribute("y2"))
                    );
                    line.setStroke(Color.web(element.getAttribute("stroke")));
                    line.setStrokeWidth(parseDouble(element.getAttribute("stroke-width")));
                    contentGroup.getChildren().add(line);
                    break;

                case "g": // Группа
                    NodeList children = element.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        processSvgNode(children.item(i));
                    }
                    break;
            }
        }
    }
    private double parseDouble(String value) throws NumberFormatException, ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(symbols);

        value = value.trim().replace(',', '.');
        return decimalFormat.parse(value).doubleValue();
    }

    public void saveToPNG(File file) throws IOException {
        WritableImage snapshot = drawingArea.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

        ImageIO.write(bufferedImage, "png", file);
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

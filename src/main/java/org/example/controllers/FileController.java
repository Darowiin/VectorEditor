package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.example.models.PathElementParser;
import org.example.models.ShapeData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import java.util.Objects;

public class FileController {
    private DrawController drawController;

    public void initialize(DrawController drawController) {
        this.drawController = drawController;
    }

    public void saveToJSON(File file) throws IOException {
        List<ShapeData> shapeDataList = new ArrayList<>();
        for (javafx.scene.Node node : drawController.getContentGroup().getChildren()) {
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
                data.setFillColor(rectangle.getFill().toString()); // Заливка
                shapeDataList.add(data);
            } else if (node instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) node;
                ShapeData data = new ShapeData();
                data.setType("ellipse");
                data.setCenterX(ellipse.getCenterX());
                data.setCenterY(ellipse.getCenterY());
                data.setRadiusX(ellipse.getRadiusX());
                data.setRadiusY(ellipse.getRadiusY());
                data.setStrokeColor(ellipse.getStroke().toString());
                data.setStrokeWidth(ellipse.getStrokeWidth());
                data.setFillColor(ellipse.getFill().toString());
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
                data.setFillColor("none");
                shapeDataList.add(data);
            } else if (node instanceof Path) {
                Path path = (Path) node;
                StringBuilder pathData = new StringBuilder();

                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo) {
                        MoveTo moveTo = (MoveTo) element;
                        pathData.append(String.format(Locale.US, "M %.2f %.2f ", moveTo.getX(), moveTo.getY()));
                    } else if (element instanceof LineTo) {
                        LineTo lineTo = (LineTo) element;
                        pathData.append(String.format(Locale.US, "L %.2f %.2f ", lineTo.getX(), lineTo.getY()));
                    } else if (element instanceof CubicCurveTo) {
                        CubicCurveTo cubic = (CubicCurveTo) element;
                        pathData.append(String.format(Locale.US, "C %.2f %.2f, %.2f %.2f, %.2f %.2f ",
                                cubic.getControlX1(), cubic.getControlY1(),
                                cubic.getControlX2(), cubic.getControlY2(),
                                cubic.getX(), cubic.getY()));
                    } else if (element instanceof QuadCurveTo) {
                        QuadCurveTo quad = (QuadCurveTo) element;
                        pathData.append(String.format(Locale.US, "Q %.2f %.2f, %.2f %.2f ",
                                quad.getControlX(), quad.getControlY(),
                                quad.getX(), quad.getY()));
                    } else if (element instanceof ClosePath) {
                        pathData.append("Z ");
                    }
                }

                ShapeData data = new ShapeData();
                data.setType("path");
                data.setPathData(pathData.toString().trim());
                data.setStrokeColor(path.getStroke().toString());
                data.setStrokeWidth(path.getStrokeWidth());
                data.setFillColor(path.getFill().toString());
                shapeDataList.add(data);
            } else if (node instanceof Circle) {
                continue;
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
            drawController.getContentGroup().getChildren().clear();
            for (ShapeData data : shapeDataList) {
                System.out.println(data.getType());
                switch (data.getType()) {
                    case "rectangle":
                        Rectangle rectangle = new Rectangle(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        rectangle.setStroke(Color.web(data.getStrokeColor()));
                        rectangle.setStrokeWidth(data.getStrokeWidth());
                        rectangle.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(rectangle);
                        drawController.enableResizing(rectangle);
                        break;
                    case "ellipse":
                        Ellipse ellipse = new Ellipse(data.getCenterX(), data.getCenterY(), data.getRadiusX(), data.getRadiusY());
                        ellipse.setStroke(Color.web(data.getStrokeColor()));
                        ellipse.setStrokeWidth(data.getStrokeWidth());
                        ellipse.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(ellipse);
                        drawController.enableResizing(ellipse);
                        break;
                    case "line":
                        Line line = new Line(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        line.setStroke(Color.web(data.getStrokeColor()));
                        line.setStrokeWidth(data.getStrokeWidth());
                        drawController.getContentGroup().getChildren().add(line);
                        drawController.enableResizing(line);
                        break;
                    case "path":
                        Path path = new Path();
                        path.getElements().addAll(PathElementParser.parseSvgPathData(data.getPathData()));
                        path.setStroke(Color.web(data.getStrokeColor()));
                        path.setStrokeWidth(data.getStrokeWidth());
                        path.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(path);
                        drawController.enableResizing(path);
                        break;
                }
            }
        }
    }

    public void saveToSvg(File file) throws IOException {
        StringBuilder svgContent = new StringBuilder();

        svgContent.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        for (javafx.scene.Node node : drawController.getContentGroup().getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle rect = (Rectangle) node;
                String fillColor = rect.getFill() == null || rect.getFill().equals(Color.TRANSPARENT)
                        ? "transparent"
                        : toHexString((Color) rect.getFill());
                svgContent.append(String.format(Locale.US,
                        "<rect x=\"%.2f\" y=\"%.2f\" width=\"%.2f\" height=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"%s\" />\n",
                        rect.getX(),
                        rect.getY(),
                        rect.getWidth(),
                        rect.getHeight(),
                        toHexString((Color) rect.getStroke()),
                        rect.getStrokeWidth(),
                        fillColor
                ));
            } else if (node instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) node;
                String fillColor = ellipse.getFill() == null || ellipse.getFill().equals(Color.TRANSPARENT)
                        ? "transparent"
                        : toHexString((Color) ellipse.getFill());
                svgContent.append(String.format(Locale.US,
                        "<ellipse cx=\"%.2f\" cy=\"%.2f\" rx=\"%.2f\" ry=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"%s\" />\n",
                        ellipse.getCenterX(),
                        ellipse.getCenterY(),
                        ellipse.getRadiusX(),
                        ellipse.getRadiusY(),
                        toHexString((Color) ellipse.getStroke()),
                        ellipse.getStrokeWidth(),
                        fillColor
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
            } else if (node instanceof Path) {
                Path path = (Path) node;
                String fillColor = path.getFill() == null || path.getFill().equals(Color.TRANSPARENT)
                        ? "transparent"
                        : toHexString((Color) path.getFill());
                StringBuilder pathData = new StringBuilder();

                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo) {
                        MoveTo moveTo = (MoveTo) element;
                        pathData.append(String.format(Locale.US, "M %.2f %.2f ", moveTo.getX(), moveTo.getY()));
                    } else if (element instanceof LineTo) {
                        LineTo lineTo = (LineTo) element;
                        pathData.append(String.format(Locale.US, "L %.2f %.2f ", lineTo.getX(), lineTo.getY()));
                    } else if (element instanceof CubicCurveTo) {
                        CubicCurveTo cubic = (CubicCurveTo) element;
                        pathData.append(String.format(Locale.US, "C %.2f %.2f, %.2f %.2f, %.2f %.2f ",
                                cubic.getControlX1(), cubic.getControlY1(),
                                cubic.getControlX2(), cubic.getControlY2(),
                                cubic.getX(), cubic.getY()));
                    } else if (element instanceof QuadCurveTo) {
                        QuadCurveTo quad = (QuadCurveTo) element;
                        pathData.append(String.format(Locale.US, "Q %.2f %.2f, %.2f %.2f ",
                                quad.getControlX(), quad.getControlY(),
                                quad.getX(), quad.getY()));
                    } else if (element instanceof ClosePath) {
                        pathData.append("Z ");
                    }
                }

                svgContent.append(String.format(Locale.US,
                        "<path d=\"%s\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"%s\" />\n",
                        pathData.toString().trim(),
                        toHexString((Color) path.getStroke()),
                        path.getStrokeWidth(),
                        fillColor
                ));
            }
        }

        svgContent.append("</svg>");

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

        drawController.getContentGroup().getChildren().clear();
        NodeList elements = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < elements.getLength(); i++) {
            processSvgNode(elements.item(i));
        }
    }

    private void processSvgNode(Node node) throws ParseException {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            Color fillColor = null;
            if ((Objects.equals(element.getTagName(), "rect")) || (Objects.equals(element.getTagName(), "circle")) || (Objects.equals(element.getTagName(), "path"))) {
                String fillAttribute = element.getAttribute("fill");
                fillColor = "none".equals(fillAttribute) ? Color.TRANSPARENT : Color.web(fillAttribute);
            }

            String strokeAttribute = element.getAttribute("stroke");
            Color strokeColor = strokeAttribute == null || strokeAttribute.isEmpty() || "none".equals(strokeAttribute)
                    ? null
                    : Color.web(strokeAttribute);

            double strokeWidth = Double.parseDouble(element.getAttribute("stroke-width"));

            switch (element.getTagName()) {
                case "rect":
                    Rectangle rect = new Rectangle(
                            parseDouble(element.getAttribute("x")),
                            parseDouble(element.getAttribute("y")),
                            parseDouble(element.getAttribute("width")),
                            parseDouble(element.getAttribute("height"))
                    );
                    rect.setStroke(strokeColor);
                    rect.setStrokeWidth(strokeWidth);
                    rect.setFill(fillColor); // Устанавливаем заливку
                    drawController.getContentGroup().getChildren().add(rect);
                    drawController.enableResizing(rect);
                    break;

                case "ellipse":
                    Ellipse ellipse = new Ellipse(
                            parseDouble(element.getAttribute("cx")),
                            parseDouble(element.getAttribute("cy")),
                            parseDouble(element.getAttribute("rx")),
                            parseDouble(element.getAttribute("ry"))
                    );
                    ellipse.setStroke(strokeColor);
                    ellipse.setStrokeWidth(strokeWidth);
                    ellipse.setFill(fillColor);
                    drawController.getContentGroup().getChildren().add(ellipse);
                    drawController.enableResizing(ellipse);
                    break;

                case "line":
                    Line line = new Line(
                            parseDouble(element.getAttribute("x1")),
                            parseDouble(element.getAttribute("y1")),
                            parseDouble(element.getAttribute("x2")),
                            parseDouble(element.getAttribute("y2"))
                    );
                    line.setStroke(strokeColor);
                    line.setStrokeWidth(strokeWidth);
                    drawController.getContentGroup().getChildren().add(line);
                    drawController.enableResizing(line);
                    break;

                case "path":
                    String pathData = element.getAttribute("d");
                    Path path = new Path();
                    path.getElements().addAll(PathElementParser.parseSvgPathData(pathData));
                    path.setStroke(strokeColor);
                    path.setStrokeWidth(strokeWidth);
                    path.setFill(fillColor);
                    drawController.getContentGroup().getChildren().add(path);
                    drawController.enableResizing(path);
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
        // Удаляем временные элементы (например, кружочки) перед созданием снимка
        List<javafx.scene.Node> toRemove = new ArrayList<>();
        for (javafx.scene.Node node : drawController.getContentGroup().getChildren()) {
            if (node instanceof Circle && "draggable-handle".equals(node.getId())) {
                toRemove.add(node);
            }
        }
        drawController.getContentGroup().getChildren().removeAll(toRemove);

        // Создаем снимок
        WritableImage snapshot = drawController.drawingArea.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);

        // Восстанавливаем удаленные элементы
        drawController.getContentGroup().getChildren().addAll(toRemove);

        ImageIO.write(bufferedImage, "png", file);
    }
}

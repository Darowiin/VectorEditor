package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.example.models.PathElementParser;
import org.example.models.ShapeData;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileController {
    private DrawController drawController;
    private ResizingController resizingController;

    public void initialize(DrawController drawController, ResizingController resizingController) {
        this.drawController = drawController;
        this.resizingController = resizingController;
    }

    public void saveToJSON(File file) throws IOException {
        List<ShapeData> shapeDataList = new ArrayList<>();
        for (javafx.scene.Node node : drawController.getContentGroup().getChildren()) {
            if (node instanceof Rectangle rectangle) {
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
            } else if (node instanceof Ellipse ellipse) {
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
            } else if (node instanceof Line line) {
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
            } else if (node instanceof Text text) {
                ShapeData data = new ShapeData();
                data.setType("text");
                data.setX(text.getX());
                data.setY(text.getY());
                data.setTextContent(text.getText());
                data.setFontSize(text.getFont().getSize());
                data.setFontWeight(text.getFont().getStyle());
                data.setTextColor(text.getFill().toString());
                shapeDataList.add(data);
            } else if (node instanceof TextArea textArea) {
                ShapeData data = new ShapeData();
                data.setType("textarea");
                data.setX(textArea.getLayoutX());
                data.setY(textArea.getLayoutY());
                data.setWidth(textArea.getPrefWidth());
                data.setHeight(textArea.getPrefHeight());
                data.setTextContent(textArea.getText());
                data.setFontSize(textArea.getFont().getSize());
                data.setTextColor(textArea.getStyle());
                shapeDataList.add(data);
            } else if (node instanceof Polygon polygon) {
                ShapeData data = new ShapeData();
                data.setType("polygon");
                data.setPoints(new ArrayList<>(polygon.getPoints()));
                data.setStrokeColor(polygon.getStroke().toString());
                data.setStrokeWidth(polygon.getStrokeWidth());
                data.setFillColor(polygon.getFill().toString());
                shapeDataList.add(data);
            } else if (node instanceof Path path) {
                StringBuilder pathData = new StringBuilder();

                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo moveTo) {
                        pathData.append(String.format(Locale.US, "M %.2f %.2f ", moveTo.getX(), moveTo.getY()));
                    } else if (element instanceof LineTo lineTo) {
                        pathData.append(String.format(Locale.US, "L %.2f %.2f ", lineTo.getX(), lineTo.getY()));
                    } else if (element instanceof CubicCurveTo cubic) {
                        pathData.append(String.format(Locale.US, "C %.2f %.2f, %.2f %.2f, %.2f %.2f ",
                                cubic.getControlX1(), cubic.getControlY1(),
                                cubic.getControlX2(), cubic.getControlY2(),
                                cubic.getX(), cubic.getY()));
                    } else if (element instanceof QuadCurveTo quad) {
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
                switch (data.getType()) {
                    case "rectangle":
                        Rectangle rectangle = new Rectangle(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        rectangle.setStroke(Color.web(data.getStrokeColor()));
                        rectangle.setStrokeWidth(data.getStrokeWidth());
                        rectangle.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(rectangle);
                        resizingController.enableResizing(rectangle);
                        break;
                    case "ellipse":
                        Ellipse ellipse = new Ellipse(data.getCenterX(), data.getCenterY(), data.getRadiusX(), data.getRadiusY());
                        ellipse.setStroke(Color.web(data.getStrokeColor()));
                        ellipse.setStrokeWidth(data.getStrokeWidth());
                        ellipse.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(ellipse);
                        resizingController.enableResizing(ellipse);
                        break;
                    case "line":
                        Line line = new Line(data.getX(), data.getY(), data.getWidth(), data.getHeight());
                        line.setStroke(Color.web(data.getStrokeColor()));
                        line.setStrokeWidth(data.getStrokeWidth());
                        drawController.getContentGroup().getChildren().add(line);
                        resizingController.enableResizing(line);
                        break;
                    case "path":
                        Path path = new Path();
                        path.getElements().addAll(PathElementParser.parseSvgPathData(data.getPathData()));
                        path.setStroke(Color.web(data.getStrokeColor()));
                        path.setStrokeWidth(data.getStrokeWidth());
                        path.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(path);
                        resizingController.enableResizing(path);
                        break;
                    case "polygon":
                        Polygon polygon = new Polygon();
                        polygon.getPoints().addAll(data.getPoints());
                        polygon.setStroke(Color.web(data.getStrokeColor()));
                        polygon.setStrokeWidth(data.getStrokeWidth());
                        polygon.setFill(Color.web(data.getFillColor()));
                        drawController.getContentGroup().getChildren().add(polygon);
                        resizingController.enableResizing(polygon);
                        break;
                    case "text":
                        Text text = new Text(data.getX(), data.getY(), data.getTextContent());
                        text.setFont(Font.font(data.getFontSize()));
                        text.setFill(Color.web(data.getTextColor()));
                        text.setFont(Font.font("Arial", data.getFontWeight(), data.getFontSize()));
                        drawController.getContentGroup().getChildren().add(text);
                        break;
                    case "textarea":
                        TextArea textArea = new TextArea(data.getTextContent());
                        textArea.setLayoutX(data.getX());
                        textArea.setLayoutY(data.getY());
                        textArea.setPrefWidth(data.getWidth());
                        textArea.setPrefHeight(data.getHeight());
                        textArea.setFont(Font.font(data.getFontSize()));
                        textArea.setStyle(data.getTextColor());
                        drawController.getContentGroup().getChildren().add(textArea);
                        break;
                }
            }
        }
    }

    public void saveToSvg(File file) throws IOException, ParseException {
        StringBuilder svgContent = new StringBuilder();

        svgContent.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");

        for (javafx.scene.Node node : drawController.getContentGroup().getChildren()) {
            if (node instanceof Rectangle rect) {
                String fillColor = rect.getFill() == null || rect.getFill().equals(Color.TRANSPARENT)
                        ? "none"
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
            } else if (node instanceof Ellipse ellipse) {
                String fillColor = ellipse.getFill() == null || ellipse.getFill().equals(Color.TRANSPARENT)
                        ? "none"
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
            } else if (node instanceof Line line) {
                svgContent.append(String.format(Locale.US,
                        "<line x1=\"%.2f\" y1=\"%.2f\" x2=\"%.2f\" y2=\"%.2f\" stroke=\"%s\" stroke-width=\"%.2f\" />\n",
                        line.getStartX(),
                        line.getStartY(),
                        line.getEndX(),
                        line.getEndY(),
                        toHexString((Color) line.getStroke()),
                        line.getStrokeWidth()
                ));
            } else if (node instanceof Polygon polygon) {
                String fillColor = polygon.getFill() == null || polygon.getFill().equals(Color.TRANSPARENT)
                        ? "none"
                        : toHexString((Color) polygon.getFill());
                StringBuilder pointsData = new StringBuilder();
                for (int i = 0; i < polygon.getPoints().size(); i += 2) {
                    pointsData.append(String.format(Locale.US, "%.2f,%.2f ",
                            polygon.getPoints().get(i), polygon.getPoints().get(i + 1)));
                }
                svgContent.append(String.format(Locale.US,
                        "<polygon points=\"%s\" stroke=\"%s\" stroke-width=\"%.2f\" fill=\"%s\" />\n",
                        pointsData.toString().trim(),
                        toHexString((Color) polygon.getStroke()),
                        polygon.getStrokeWidth(),
                        fillColor
                ));
            } else if (node instanceof Path path) {
                String fillColor = path.getFill() == null || path.getFill().equals(Color.TRANSPARENT)
                        ? "none"
                        : toHexString((Color) path.getFill());
                StringBuilder pathData = new StringBuilder();

                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo moveTo) {
                        pathData.append(String.format(Locale.US, "M %.2f %.2f ", moveTo.getX(), moveTo.getY()));
                    } else if (element instanceof LineTo lineTo) {
                        pathData.append(String.format(Locale.US, "L %.2f %.2f ", lineTo.getX(), lineTo.getY()));
                    } else if (element instanceof CubicCurveTo cubic) {
                        pathData.append(String.format(Locale.US, "C %.2f %.2f, %.2f %.2f, %.2f %.2f ",
                                cubic.getControlX1(), cubic.getControlY1(),
                                cubic.getControlX2(), cubic.getControlY2(),
                                cubic.getX(), cubic.getY()));
                    } else if (element instanceof QuadCurveTo quad) {
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
            } else if (node instanceof Text text) {
                String fillColor = text.getFill() == null || text.getFill().equals(Color.TRANSPARENT)
                        ? "#000000"
                        : toHexString((Color) text.getFill());
                String fontStyle = text.getFont().getStyle();

                String fontWeightString;
                if (fontStyle.contains("Light")) {
                    fontWeightString = "light";
                } else if (fontStyle.contains("Bold")) {
                    fontWeightString = "bold";
                } else {
                    fontWeightString = "normal";
                }
                svgContent.append(String.format(Locale.US,
                        "<text x=\"%.2f\" y=\"%.2f\" font-size=\"%.2f\" font-weight=\"%s\" fill=\"%s\" >%s</text>\n",
                        text.getX(),
                        text.getY(),
                        text.getFont().getSize(),
                        fontWeightString,
                        fillColor,
                        text.getText()
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
            if ((Objects.equals(element.getTagName(), "rect")) ||
                    (Objects.equals(element.getTagName(), "ellipse")) ||
                    (Objects.equals(element.getTagName(), "path")) ||
                    (Objects.equals(element.getTagName(), "polygon")) ||
                    (Objects.equals(element.getTagName(), "text")))
            {
                String fillAttribute = element.getAttribute("fill");
                fillColor = "none".equals(fillAttribute) ? Color.TRANSPARENT : Color.web(fillAttribute);
            }

            String strokeAttribute = element.getAttribute("stroke");
            Color strokeColor = strokeAttribute == null || strokeAttribute.isEmpty() || "none".equals(strokeAttribute)
                    ? null
                    : Color.web(strokeAttribute);

            String strokeWidthAttribute = element.getAttribute("stroke-width");
            double strokeWidth = strokeWidthAttribute == null || strokeWidthAttribute.isEmpty() ? 0.0 : Double.parseDouble(strokeWidthAttribute);

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
                    resizingController.enableResizing(rect);
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
                    resizingController.enableResizing(ellipse);
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
                    resizingController.enableResizing(line);
                    break;

                case "path":
                    String pathData = element.getAttribute("d");
                    Path path = new Path();
                    path.getElements().addAll(PathElementParser.parseSvgPathData(pathData));
                    path.setStroke(strokeColor);
                    path.setStrokeWidth(strokeWidth);
                    path.setFill(fillColor);
                    drawController.getContentGroup().getChildren().add(path);
                    resizingController.enableResizing(path);
                    break;

                case "polygon":
                    String points = element.getAttribute("points");
                    Polygon polygon = new Polygon();
                    String[] pointsArray = points.split("\\s+");
                    for (String point : pointsArray) {
                        String[] coordinates = point.split(",");
                        if (coordinates.length == 2) {
                            polygon.getPoints().add(parseDouble(coordinates[0]));
                            polygon.getPoints().add(parseDouble(coordinates[1]));
                        }
                    }
                    polygon.setStroke(strokeColor);
                    polygon.setStrokeWidth(strokeWidth);
                    polygon.setFill(fillColor);
                    drawController.getContentGroup().getChildren().add(polygon);
                    resizingController.enableResizing(polygon);
                    break;

                case "text":
                    double x = parseDouble(element.getAttribute("x"));
                    double y = parseDouble(element.getAttribute("y"));

                    String textContent = element.getTextContent().trim();

                    Text text = new Text(x, y, textContent);

                    String fontWeight = element.getAttribute("font-weight");
                    FontWeight weight = parseFontWeight(fontWeight);

                    double fontSize = parseDouble(element.getAttribute("font-size"));
                    text.setFont(Font.font("Arial", weight, fontSize));

                    String fillAttribute = element.getAttribute("fill");
                    fillColor = "none".equals(fillAttribute) ? Color.BLACK : Color.web(fillAttribute);
                    text.setFill(fillColor);

                    drawController.getContentGroup().getChildren().add(text);
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

    private FontWeight parseFontWeight(String fontWeight) {
        if (fontWeight == null || fontWeight.isEmpty()) {
            return FontWeight.NORMAL;
        }

        return switch (fontWeight.toLowerCase()) {
            case "100", "thin" -> FontWeight.THIN;
            case "200", "extra-light", "ultra-light" -> FontWeight.EXTRA_LIGHT;
            case "300", "light" -> FontWeight.LIGHT;
            case "400", "normal" -> FontWeight.NORMAL;
            case "500", "medium" -> FontWeight.MEDIUM;
            case "600", "semi-bold", "demi-bold" -> FontWeight.SEMI_BOLD;
            case "700", "bold" -> FontWeight.BOLD;
            case "800", "extra-bold", "ultra-bold" -> FontWeight.EXTRA_BOLD;
            case "900", "black", "heavy" -> FontWeight.BLACK;
            default -> FontWeight.NORMAL;
        };
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
            if (node instanceof Circle) {
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

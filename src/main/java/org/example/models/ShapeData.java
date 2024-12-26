package org.example.models;

import javafx.scene.text.FontWeight;

import java.util.List;

public class ShapeData {
    private String type;
    private double x;
    private double y;
    private double width;
    private double height;
    private double centerX;
    private double centerY;
    private double radiusX;
    private double radiusY;
    private String strokeColor;
    private String fillColor;
    private double strokeWidth;
    private String pathData;
    private List<Double> points;
    private String textContent;
    private double fontSize;
    private String textColor;
    private FontWeight fontWeight;

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getWidth() {
        return width;
    }
    public void setWidth(double width) {
        this.width = width;
    }
    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }
    public double getCenterX() {
        return centerX;
    }
    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }
    public double getCenterY() {
        return centerY;
    }
    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }
    public double getRadiusX() {
        return radiusX;
    }
    public void setRadiusX(double radiusX) {
        this.radiusX = radiusX;
    }
    public double getRadiusY() {
        return radiusY;
    }
    public void setRadiusY(double radiusY) {
        this.radiusY = radiusY;
    }
    public String getStrokeColor() {
        return strokeColor;
    }
    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }
    public String getFillColor() {return fillColor; }
    public void setFillColor(String fillColor) {this.fillColor = fillColor; }
    public double getStrokeWidth() {
        return strokeWidth;
    }
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    public String getPathData() { return pathData; }
    public void setPathData(String pathData) { this.pathData = pathData; }
    public List<Double> getPoints() {
        return points;
    }
    public void setPoints(List<Double> points) {
        this.points = points;
    }
    public String getTextContent() {
        return textContent;
    }
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
    public double getFontSize() {
        return fontSize;
    }
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }
    public FontWeight getFontWeight() {
        return fontWeight;
    }
    public void setFontWeight(String fontWeight) {
        if (fontWeight == null || fontWeight.isEmpty()) {
            this.fontWeight =  FontWeight.NORMAL;
        }

        switch (fontWeight.toLowerCase()) {
            case "300", "light" -> this.fontWeight = FontWeight.LIGHT;
            case "400", "normal" -> this.fontWeight = FontWeight.NORMAL;
            case "700", "bold" -> this.fontWeight = FontWeight.BOLD;
            default -> this.fontWeight = FontWeight.NORMAL;
        };
    }
}

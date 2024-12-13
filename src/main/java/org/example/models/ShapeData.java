package org.example.models;

public class ShapeData {
    private String type;
    private double x;
    private double y;
    private double width;
    private double height;
    private double centerX;
    private double centerY;
    private double radius;
    private String strokeColor;
    private double strokeWidth;

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
    public double getRadius() {
        return radius;
    }
    public void setRadius(double radius) {
        this.radius = radius;
    }
    public String getStrokeColor() {
        return strokeColor;
    }
    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }
    public double getStrokeWidth() {
        return strokeWidth;
    }
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}

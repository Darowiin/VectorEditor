package org.example.controllers;

import javafx.scene.paint.Color;

public class ColorController {
    private Color strokeColor = Color.BLACK;
    private Color fillColor = Color.TRANSPARENT;

    public Color getCurrentColor() {
        return strokeColor;
    }

    public void setCurrentColor(Color color) {
        this.strokeColor = color;
        System.out.println("Selected stroke color: " + color.toString());
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
        System.out.println("Selected fill color: " + color.toString());
    }
}
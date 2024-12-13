package org.example.controllers;

import javafx.scene.paint.Color;

public class ColorController {
    private Color currentColor = Color.BLACK;

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        System.out.println("Selected color: " + color.toString());
    }
}

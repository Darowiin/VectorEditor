package org.example.models;

import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

public class LayerModel {

    private String name;                 // Имя слоя
    private boolean visible;             // Видимость слоя
    private boolean locked;              // Блокировка слоя
    private final List<Node> shapes;     // Список фигур (например, Rectangle, Circle и т.д.)

    public LayerModel(String name) {
        this.name = name;
        this.visible = true;             // По умолчанию слой видимый
        this.locked = false;             // По умолчанию слой не заблокирован
        this.shapes = new ArrayList<>();
    }

    // Получить имя слоя
    public String getName() {
        return name;
    }

    // Установить имя слоя
    public void setName(String name) {
        this.name = name;
    }

    // Проверка видимости слоя
    public boolean isVisible() {
        return visible;
    }

    // Установить видимость слоя
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // Проверка блокировки слоя
    public boolean isLocked() {
        return locked;
    }

    // Установить блокировку слоя
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // Получить список фигур
    public List<Node> getShapes() {
        return shapes;
    }

    // Добавить фигуру на слой
    public void addShape(Node shape) {
        if (!locked) {                   // Если слой не заблокирован
            shapes.add(shape);
        } else {
            throw new IllegalStateException("Cannot add shapes to a locked layer.");
        }
    }

    // Удалить фигуру со слоя
    public void removeShape(Node shape) {
        if (!locked) {                   // Если слой не заблокирован
            shapes.remove(shape);
        } else {
            throw new IllegalStateException("Cannot remove shapes from a locked layer.");
        }
    }

    // Очистить слой
    public void clearShapes() {
        if (!locked) {                   // Если слой не заблокирован
            shapes.clear();
        } else {
            throw new IllegalStateException("Cannot clear a locked layer.");
        }
    }
}

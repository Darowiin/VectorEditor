package org.example.controllers;

import org.example.models.LayerModel;

import java.util.ArrayList;
import java.util.List;

public class LayerController {
    private final List<LayerModel> layers = new ArrayList<>();
    private LayerModel currentLayer;

    public void addLayer(String name) {
        LayerModel newLayer = new LayerModel(name);
        layers.add(newLayer);
        currentLayer = newLayer;
        System.out.println("Added layer: " + name);
    }

    public void removeLayer(String name) {
        layers.removeIf(layer -> layer.getName().equals(name));
        System.out.println("Removed layer: " + name);
    }

    public List<LayerModel> getLayers() {
        return layers;
    }

    public LayerModel getCurrentLayer() {
        return currentLayer;
    }
}

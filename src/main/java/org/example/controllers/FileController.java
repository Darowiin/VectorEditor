package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.models.ShapeData;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class FileController {
    private final Gson gson = new Gson();

    public List<ShapeData> loadFromFile(File file) throws IOException {
        Type listType = new TypeToken<List<ShapeData>>() {}.getType();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, listType);
        }
    }

    public void saveToFile(File file, List<ShapeData> shapes) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(shapes, writer);
        }
    }
}

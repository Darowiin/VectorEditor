package org.example.models;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class PathElementParser {
    public static List<PathElement> parseSvgPathData(String pathData) {
        List<PathElement> elements = new ArrayList<>();
        String[] tokens = pathData.split("(?=[MLCQZ])");

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }

            char command = token.charAt(0);
            String[] coords = token.substring(1).trim().split("[ ,]+");

            if (coords.length == 0) {
                continue;
            }

            switch (command) {
                case 'M': // MoveTo
                    if (coords.length >= 2) {
                        elements.add(new MoveTo(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
                    }
                    break;
                case 'L': // LineTo
                    if (coords.length >= 2) {
                        elements.add(new LineTo(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));
                    }
                    break;
                case 'C': // CubicCurveTo
                    if (coords.length >= 6) {
                        elements.add(new CubicCurveTo(
                                Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), // controlX1, controlY1
                                Double.parseDouble(coords[2]), Double.parseDouble(coords[3]), // controlX2, controlY2
                                Double.parseDouble(coords[4]), Double.parseDouble(coords[5])  // x, y
                        ));
                    }
                    break;
                case 'Q': // QuadCurveTo
                    if (coords.length >= 4) {
                        elements.add(new QuadCurveTo(
                                Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), // controlX, controlY
                                Double.parseDouble(coords[2]), Double.parseDouble(coords[3])  // x, y
                        ));
                    }
                    break;
                case 'Z': // ClosePath
                    elements.add(new ClosePath());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown SVG path command: " + command);
            }
        }

        return elements;
    }
}
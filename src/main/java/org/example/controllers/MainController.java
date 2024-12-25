package org.example.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.example.enums.ToolMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

public class MainController {

    @FXML private MenuItem newFileMenuItem;
    @FXML private MenuItem openFileMenuItem;
    @FXML private MenuItem saveFileMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem undoMenuItem;
    @FXML private MenuItem redoMenuItem;
    @FXML private MenuItem deleteMenuItem;
    @FXML private MenuItem zoomInMenuItem;
    @FXML private MenuItem zoomOutMenuItem;
    @FXML private MenuItem aboutMenuItem;

    @FXML private Button selectToolButton;
    @FXML private Button areaToolButton;
    @FXML private Button moveToolButton;
    @FXML private Button rectangleToolButton;
    @FXML private Button ellipseToolButton;
    @FXML private Button lineToolButton;
    @FXML private Button curveToolButton;
    @FXML private Button polygonToolButton;
    @FXML private Button textToolButton;
    @FXML private Button colorBlack;
    @FXML private Button colorRed;
    @FXML private Button colorBlue;
    @FXML private Button colorGreen;
    @FXML private Button colorYellow;
    @FXML private ColorPicker colorPicker;
    @FXML private ColorPicker fillColorPicker;
    @FXML private Slider strokeWidthSlider;
    @FXML private Label strokeWidthValueLabel;
    @FXML private ComboBox<String> fontSize;
    @FXML private Button eyedropperToolButton;

    @FXML private ScrollPane drawingScrollPane;

    @FXML private Pane drawingArea;

    @FXML private Label statusBar;

    private ToolController toolController;
    private ColorController colorController;
    private DrawController drawController;
    private FileController fileController;
    private HistoryController historyController;
    private ResizingController resizingController;

    private double scaleFactor = 1.0; // Текущий масштаб
    private static final double ZOOM_STEP = 0.1; // Шаг изменения масштаба
    private static final double MAX_SCALE = 5.0; // Максимальный масштаб
    private static final double MIN_SCALE = 1.0; // Минимальный масштаб
    private boolean isEyedropperActive = false;
    protected static double fontSizeValue = 16.0;

    private final EventHandler<WindowEvent> closeEventHandler = event -> {
        if (DrawController.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before exiting?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                    System.exit(0);
                } else if (result.get() == discardButton) {
                    System.exit(0);
                } else if (result.get() == cancelButton) {
                    event.consume();
                }
            } else {
                event.consume();
            }
        } else {
            System.exit(0);
        }
    };

    public EventHandler<WindowEvent> getCloseEventHandler(){
        return closeEventHandler;
    }

    @FXML
    public void initialize() {
        toolController = new ToolController();
        colorController = new ColorController();
        drawController = new DrawController();
        fileController = new FileController();
        historyController = new HistoryController();
        resizingController = new ResizingController();

        selectToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.SELECT);
            statusBar.setText("Tool: Select");
        });
        areaToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.AREA);
            statusBar.setText("Tool: Area");
        });
        moveToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.MOVE);
            statusBar.setText("Tool: Move");
        });
        rectangleToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.RECTANGLE);
            statusBar.setText("Tool: Rectangle");
        });
        ellipseToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.ELLIPSE);
            statusBar.setText("Tool: Circle");
        });
        lineToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.LINE);
            statusBar.setText("Tool: Line");
        });
        curveToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.CURVE);
            statusBar.setText("Tool: Curve");
        });
        polygonToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.POLYGON);
            statusBar.setText("Tool: Polygon");
        });
        textToolButton.setOnAction(event -> {
            toolController.setCurrentTool(ToolMode.TEXT);
            statusBar.setText("Tool: Text");
        });
        colorBlack.setOnAction(event -> {
            colorController.setCurrentColor(Color.BLACK);
            statusBar.setText("Color: Black");
        });
        colorRed.setOnAction(event -> {
            colorController.setCurrentColor(Color.RED);
            statusBar.setText("Color: Red");
        });
        colorBlue.setOnAction(event -> {
            colorController.setCurrentColor(Color.BLUE);
            statusBar.setText("Color: Blue");
        });
        colorGreen.setOnAction(event -> {
            colorController.setCurrentColor(Color.GREEN);
            statusBar.setText("Color: Green");
        });
        colorYellow.setOnAction(event -> {
            colorController.setCurrentColor(Color.YELLOW);
            statusBar.setText("Color: Yellow");
        });

        colorPicker.setOnAction(event -> {
            Color selectedColor = colorPicker.getValue();
            colorController.setCurrentColor(selectedColor);
            statusBar.setText("Color: " + selectedColor.toString());
        });
        this.fillColorPicker.setValue(colorController.getFillColor());
        this.fillColorPicker.setOnAction(event ->
                colorController.setFillColor(fillColorPicker.getValue())
        );

        drawController.initialize(drawingArea, toolController, colorController, historyController, resizingController);
        resizingController.initialize(toolController,drawController, historyController);
        fileController.initialize(drawController, resizingController);
        fontSize.getSelectionModel().select(2);
        fontSize.setOnAction(event -> {
            String newValue = fontSize.getSelectionModel().getSelectedItem();
            fontSizeValue = Double.parseDouble(newValue);
        });

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(drawingArea.widthProperty());
        clip.heightProperty().bind(drawingArea.heightProperty());
        drawingArea.setClip(clip);

        drawController.resetModificationStatus();

        newFileMenuItem.setOnAction(event -> handleNewFile());
        openFileMenuItem.setOnAction(event -> handleOpenFile());
        saveFileMenuItem.setOnAction(event -> handleSaveFile());
        drawingArea.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.isControlDown() && event.getCode().toString().equals("S")) {
                        handleSaveFile();
                        statusBar.setText("File saved using Ctrl+S.");
                        event.consume();
                    } else if (event.isControlDown() && event.getCode().toString().equals("O")) {
                        handleOpenFile();
                        statusBar.setText("File opened using Ctrl+O.");
                        event.consume();
                    } else if (event.isControlDown() && event.getCode().toString().equals("N")) {
                        handleNewFile();
                        statusBar.setText("New file created using Ctrl+N.");
                        event.consume();
                    } else if (event.isControlDown() && event.getCode().toString().equals("Z")) {
                        undo();
                        statusBar.setText("Undo using Ctrl+Z.");
                    } else if (event.isControlDown() && event.getCode().toString().equals("Y")) {
                        redo();
                        statusBar.setText("Redo using Ctrl+Y.");
                    }
                });
            }
        });

        exitMenuItem.setOnAction(event -> handleExit());

        undoMenuItem.setOnAction(event -> undo());
        redoMenuItem.setOnAction(event -> redo());
        deleteMenuItem.setOnAction(event -> {
            statusBar.setText("Delete mode activated. Click on a shape to delete it. Right mouse button to deactivate.");

            drawingArea.setOnMouseClicked(mouseEvent -> {
                drawController.deactivateDrawingHandlers();
                Node pickedNode = mouseEvent.getPickResult().getIntersectedNode();

                if (pickedNode instanceof Shape shape) {
                    int index = drawController.contentGroup.getChildren().indexOf(shape);

                    historyController.addAction(
                            () -> { // Undo - восстановить фигуру
                                drawController.contentGroup.getChildren().add(index, shape);
                            },
                            () -> { // Redo - удалить фигуру
                                drawController.contentGroup.getChildren().remove(shape);
                            }
                    );

                    drawController.contentGroup.getChildren().remove(shape);
                    statusBar.setText("Shape deleted.");
                } else {
                    statusBar.setText("No shape selected for deletion.");
                }
            });

            drawingArea.setOnMousePressed(mouseEvent -> {
                if (mouseEvent.isSecondaryButtonDown()) {
                    drawingArea.setOnMouseClicked(null);
                    drawingArea.setOnMousePressed(null);
                    drawController.activateDrawingHandlers();
                    statusBar.setText("Delete mode deactivated.");
                }
            });
        });

        zoomInMenuItem.setOnAction(event -> {
            if (scaleFactor < MAX_SCALE) {
                scaleFactor += ZOOM_STEP;
                applyZoom();
                statusBar.setText("Zoom In: " + (int) (scaleFactor * 100) + "%");
            } else {
                statusBar.setText("Maximum zoom level reached.");
            }
        });

        zoomOutMenuItem.setOnAction(event -> {
            if (scaleFactor > MIN_SCALE) {
                scaleFactor -= ZOOM_STEP;
                applyZoom();
                statusBar.setText("Zoom Out: " + (int) (scaleFactor * 100) + "%");
            } else {
                statusBar.setText("Minimum zoom level reached.");
            }
        });

        drawingArea.setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    if (scaleFactor < MAX_SCALE) {
                        scaleFactor += ZOOM_STEP;
                        applyZoom();
                        statusBar.setText("Zoom In: " + (int) (scaleFactor * 100) + "%");
                    } else {
                        statusBar.setText("Maximum zoom level reached.");
                    }
                } else if (event.getDeltaY() < 0) {
                    if (scaleFactor > MIN_SCALE) {
                        scaleFactor -= ZOOM_STEP;
                        applyZoom();
                        statusBar.setText("Zoom Out: " + (int) (scaleFactor * 100) + "%");
                    } else {
                        statusBar.setText("Minimum zoom level reached.");
                    }
                }
                event.consume();
            }
        });

        strokeWidthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double strokeWidth = newValue.doubleValue();
            strokeWidthValueLabel.setText(String.format("%.1f", strokeWidth));

            // Применить ширину к текущему инструменту или выделенной фигуре
            drawController.setStrokeWidth(strokeWidth);
            statusBar.setText("Stroke width: " + strokeWidth);
        });

        eyedropperToolButton.setOnAction(event -> {
            activateEyedropperTool();

        });

        areaToolButton.setGraphic(createImageView("area.png"));
        Tooltip areaTooltip = new Tooltip("Выделить область");
        areaTooltip.setShowDelay(Duration.millis(200));
        areaToolButton.setTooltip(areaTooltip);

        moveToolButton.setGraphic(createImageView("move.png"));
        Tooltip moveTooltip = new Tooltip("Перемещение");
        moveTooltip.setShowDelay(Duration.millis(200));
        moveToolButton.setTooltip(moveTooltip);

        eyedropperToolButton.setGraphic(createImageView("eyedropper.png"));
        Tooltip eyedropperTooltip = new Tooltip("Пипетка");
        eyedropperTooltip.setShowDelay(Duration.millis(200));
        eyedropperToolButton.setTooltip(eyedropperTooltip);

        rectangleToolButton.setGraphic(createImageView("rectangle.png"));
        Tooltip rectangleTooltip = new Tooltip("Прямоугольник");
        rectangleTooltip.setShowDelay(Duration.millis(200));
        rectangleToolButton.setTooltip(rectangleTooltip);

        ellipseToolButton.setGraphic(createImageView("ellipse.png"));
        Tooltip ellipseTooltip = new Tooltip("Эллипс");
        ellipseTooltip.setShowDelay(Duration.millis(200));
        ellipseToolButton.setTooltip(ellipseTooltip);

        lineToolButton.setGraphic(createImageView("line.png"));
        Tooltip lineTooltip = new Tooltip("Линия");
        lineTooltip.setShowDelay(Duration.millis(200));
        lineToolButton.setTooltip(lineTooltip);

        curveToolButton.setGraphic(createImageView("curve.png"));
        Tooltip curveTooltip = new Tooltip("Кривая");
        curveTooltip.setShowDelay(Duration.millis(200));
        curveToolButton.setTooltip(curveTooltip);

        polygonToolButton.setGraphic(createImageView("polygon.png"));
        Tooltip polygonTooltip = new Tooltip("Ломаная");
        polygonTooltip.setShowDelay(Duration.millis(200));
        polygonToolButton.setTooltip(polygonTooltip);

        textToolButton.setGraphic(createImageView("text.png"));
        Tooltip textTooltip = new Tooltip("Текст");
        textTooltip.setShowDelay(Duration.millis(200));
        textToolButton.setTooltip(textTooltip);

        aboutMenuItem.setOnAction(event -> statusBar.setText("About: Vectorium v1.0"));
    }

    private void handleNewFile() {
        if (DrawController.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before opening a new file?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                } else if (result.get() == discardButton) {
                    drawController.clearCanvas();
                    drawController.resetModificationStatus();
                    statusBar.setText("New file created.");
                }
            }
        } else {
            drawController.clearCanvas();
            drawController.resetModificationStatus();
            statusBar.setText("New file created.");
        }

    }

    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        // Добавление фильтров для выбора расширений
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("SVG Files", "*.svg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Показ диалогового окна выбора файла
        File file = fileChooser.showOpenDialog(drawingArea.getScene().getWindow());
        if (file != null) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json")) {
                    fileController.loadFromJSON(file);
                } else if (fileName.endsWith(".svg")) {
                    fileController.loadFromSvg(reader);
                } else {
                    throw new IllegalArgumentException("Unsupported file format: " + file.getName());
                }
                drawController.resetModificationStatus();
                statusBar.setText("File loaded: " + file.getName());
            } catch (IOException | ParseException | IllegalArgumentException e) {
                statusBar.setText("Failed to load file: " + e.getMessage());
            }
        }
    }

    private void handleSaveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        // Добавление фильтров для выбора расширений
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("SVG Files", "*.svg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );

        // Показ диалогового окна сохранения файла
        File file = fileChooser.showSaveDialog(drawingArea.getScene().getWindow());
        if (file != null) {
            try {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".json")) {
                    fileController.saveToJSON(file);
                } else if (fileName.endsWith(".svg")) {
                    fileController.saveToSvg(file);
                } else if (fileName.endsWith(".png")) {
                    fileController.saveToPNG(file);
                } else {
                    throw new IllegalArgumentException("Unsupported file format: " + file.getName());
                }
                drawController.resetModificationStatus();
                statusBar.setText("File saved: " + file.getName());
            } catch (IOException | IllegalArgumentException e) {
                statusBar.setText("Failed to save file: " + e.getMessage());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void handleExit() {
        if (DrawController.isModified()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before exiting?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    handleSaveFile();
                } else if (result.get() == discardButton) {
                    System.exit(0);
                }
            }
        } else {
            System.exit(0);
        }
    }

    public void undo() {
        statusBar.setText("Undo");
        historyController.undo();
    }

    public void redo() {
        statusBar.setText("Redo");
        historyController.redo();
    }

    private void applyZoom() {
        drawController.getContentGroup().setScaleX(scaleFactor);
        drawController.getContentGroup().setScaleY(scaleFactor);
    }

    private void activateEyedropperTool() {
        isEyedropperActive = true;
        statusBar.setText("Tool: Eyedropper");
        ToolMode tmp = toolController.getCurrentTool();
        toolController.setCurrentTool(ToolMode.EYEDROPPER);

        drawController.deactivateDrawingHandlers();

        drawingArea.setOnMousePressed(mouseEvent -> {
            Node pickedNode = mouseEvent.getPickResult().getIntersectedNode();
            if (pickedNode instanceof Shape shape) {

                if (mouseEvent.isPrimaryButtonDown()) {
                    // ЛКМ: выбираем цвет обводки
                    Color strokeColor = (Color) shape.getStroke();
                    if (mouseEvent.isControlDown()) {
                        shape.setStroke(colorPicker.getValue());
                    }
                    else if (strokeColor != null) {
                        colorPicker.setValue(strokeColor);
                        colorController.setCurrentColor(colorPicker.getValue());
                        statusBar.setText("Stroke color picked: " + colorToHex(strokeColor));
                    }
                } else if (mouseEvent.isSecondaryButtonDown()) {
                    // ПКМ: выбираем цвет заливки
                    Paint fillPaint = shape.getFill();
                    if (mouseEvent.isControlDown()) {
                        shape.setFill(fillColorPicker.getValue());
                    }
                    else if (fillPaint instanceof Color fillColor) {
                        fillColorPicker.setValue(fillColor);
                        colorController.setFillColor(fillColor);
                        statusBar.setText("Fill color picked: " + colorToHex(fillColor));
                    }
                }
            }
            deactivateEyedropperTool(tmp);
        });
    }

    private void deactivateEyedropperTool(ToolMode tmp) {
        isEyedropperActive = false;
        toolController.setCurrentTool(tmp);
        drawController.activateDrawingHandlers();
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private ImageView createImageView(String imagePath) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/" + imagePath)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        imageView.setPreserveRatio(true);
        return imageView;
    }

}
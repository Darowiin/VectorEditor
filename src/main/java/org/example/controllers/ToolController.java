package org.example.controllers;

import org.example.enums.ToolMode;

public class ToolController {
    private ToolMode currentTool = ToolMode.SELECT;

    public ToolMode getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(ToolMode tool) {
        this.currentTool = tool;
        System.out.println("Selected tool: " + tool);
    }
}
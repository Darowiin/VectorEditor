package org.example.controllers;

import java.util.Stack;

public class HistoryController {
    private final Stack<Runnable> undoStack = new Stack<>();
    private final Stack<Runnable> redoStack = new Stack<>();

    public void addAction(Runnable undoAction, Runnable redoAction) {
        undoStack.push(undoAction);
        redoStack.push(redoAction);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
            System.out.println("Undo action executed.");
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            redoStack.pop().run();
            System.out.println("Redo action executed.");
        }
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }
}

package org.example.controllers;

import java.util.Stack;

public class HistoryController {
    private final Stack<Action> undoStack = new Stack<>();
    private final Stack<Action> redoStack = new Stack<>();

    // Вспомогательный класс для хранения пар undo/redo действий
    private static class Action {
        final Runnable undoAction;
        final Runnable redoAction;

        public Action(Runnable undoAction, Runnable redoAction) {
            this.undoAction = undoAction;
            this.redoAction = redoAction;
        }
    }

    // Добавляем новое действие в историю
    public void addAction(Runnable undoAction, Runnable redoAction) {
        undoStack.push(new Action(undoAction, redoAction));
        redoStack.clear(); // Очистить redoStack при добавлении нового действия
    }

    // Выполнение Undo
    public void undo() {
        if (!undoStack.isEmpty()) {
            Action action = undoStack.pop();
            action.undoAction.run();
            redoStack.push(action); // Добавляем в redoStack
            System.out.println("Undo action executed.");
        } else {
            System.out.println("No actions to undo.");
        }
    }

    // Выполнение Redo
    public void redo() {
        if (!redoStack.isEmpty()) {
            Action action = redoStack.pop();
            action.redoAction.run();
            undoStack.push(action); // Добавляем обратно в undoStack
            System.out.println("Redo action executed.");
        } else {
            System.out.println("No actions to redo.");
        }
    }
}
package com.yandex.kanban.server;

import com.yandex.kanban.exception.BadRequestFormatException;
import com.yandex.kanban.exception.TimeConflictException;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.service.TaskManager;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendError(exchange, 405, "Метод не поддерживается");
            }
        } catch (TimeConflictException e) {
            sendHasInteractions(exchange);
        } catch (BadRequestFormatException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if (path.equals("/subtasks")) {
            String json = gson.toJson(manager.getAllSubtasks());
            sendResponse(exchange, json, 200);
        } else {
            try {
                int id = parsePathId(path);
                Optional<Subtask> subtask = manager.getSubtask(id);
                if (subtask.isPresent()) {
                    sendResponse(exchange, gson.toJson(subtask.get()), 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (BadRequestFormatException e) {
                sendBadRequest(exchange, e.getMessage());
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String json = readRequestBody(exchange);
            Subtask subtask = parseJson(json, Subtask.class);

            if (subtask.getName() == null || subtask.getName().isBlank()) {
                throw new BadRequestFormatException("Имя подзадачи обязательно");
            }

            if (subtask.getId() == 0) {
                int newId = manager.createSubtask(subtask);
                subtask.setId(newId);
                sendResponse(exchange, gson.toJson(subtask), 201);
            } else {
                manager.updateSubtask(subtask);
                sendResponse(exchange, gson.toJson(subtask), 200);
            }
        } catch (JsonSyntaxException e) {
            throw new BadRequestFormatException("Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if (path.equals("/subtasks")) {
            manager.deleteAllSubtasks();
            sendResponse(exchange, "", 200);
        } else {
            try {
                int id = parsePathId(path);
                if (manager.getSubtask(id).isPresent()) {
                    manager.deleteSubtask(id);
                    sendResponse(exchange, "", 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (BadRequestFormatException e) {
                sendBadRequest(exchange, e.getMessage());
            }
        }
    }
}
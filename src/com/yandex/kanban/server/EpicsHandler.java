package com.yandex.kanban.server;

import com.yandex.kanban.exception.BadRequestFormatException;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.service.TaskManager;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
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
        } catch (BadRequestFormatException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());

        if (path.equals("/epics")) {
            String json = gson.toJson(manager.getAllEpics());
            sendResponse(exchange, json, 200);
        } else if (path.contains("/subtasks")) {
            try {
                String basePath = path.substring(0, path.indexOf("/subtasks"));
                basePath = normalizePath(basePath);

                int epicId = parsePathId(basePath);
                Optional<Epic> epic = manager.getEpic(epicId);

                if (epic.isPresent()) {
                    String json = gson.toJson(manager.getEpicSubtasks(epicId));
                    sendResponse(exchange, json, 200);
                } else {
                    sendNotFound(exchange);
                }
            } catch (BadRequestFormatException e) {
                sendBadRequest(exchange, e.getMessage());
            }
        } else {
            try {
                int id = parsePathId(path);
                Optional<Epic> epic = manager.getEpic(id);
                if (epic.isPresent()) {
                    sendResponse(exchange, gson.toJson(epic.get()), 200);
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
            Epic epic = parseJson(json, Epic.class);

            if (epic.getName() == null || epic.getName().isBlank()) {
                throw new BadRequestFormatException("Имя эпика обязательно");
            }

            if (epic.getId() == 0) {
                int newId = manager.createEpic(epic);
                epic.setId(newId);
                sendResponse(exchange, gson.toJson(epic), 201);
            } else {
                manager.updateEpic(epic);
                sendResponse(exchange, gson.toJson(epic), 200);
            }
        } catch (JsonSyntaxException e) {
            throw new BadRequestFormatException("Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if (path.equals("/epics")) {
            manager.deleteAllEpics();
            sendResponse(exchange, "", 200);
        } else {
            try {
                int id = parsePathId(path);
                if (manager.getEpic(id).isPresent()) {
                    manager.deleteEpic(id);
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
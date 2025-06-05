package com.yandex.kanban.server;

import com.yandex.kanban.exception.BadRequestFormatException;
import com.yandex.kanban.exception.TimeConflictException;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
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
        if (path.equals("/tasks")) {
            String json = gson.toJson(manager.getAllTasks());
            sendResponse(exchange, json, 200);
        } else {
            try {
                int id = parsePathId(path);
                Optional<Task> task = manager.getTask(id);
                if (task.isPresent()) {
                    sendResponse(exchange, gson.toJson(task.get()), 200);
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
            Task task = parseJson(json, Task.class);

            if (task.getName() == null || task.getName().isBlank()) {
                throw new BadRequestFormatException("Имя задачи обязательно");
            }

            if (task.getId() == 0) {
                int newId = manager.createTask(task);
                task.setId(newId);
                sendResponse(exchange, gson.toJson(task), 201);
            } else {
                manager.updateTask(task);
                sendResponse(exchange, gson.toJson(task), 200);
            }
        } catch (JsonSyntaxException e) {
            throw new BadRequestFormatException("Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        if (path.equals("/tasks")) {
            manager.deleteAllTasks();
            sendResponse(exchange, "", 200);
        } else {
            try {
                int id = parsePathId(path);
                if (manager.getTask(id).isPresent()) {
                    manager.deleteTask(id);
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
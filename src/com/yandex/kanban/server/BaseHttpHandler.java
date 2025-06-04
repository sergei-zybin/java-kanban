package com.yandex.kanban.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.kanban.exception.BadRequestFormatException;
import com.yandex.kanban.util.DurationAdapter;
import com.yandex.kanban.util.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }

    protected <T> T parseJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gson.fromJson(json, classOfT);
    }

    protected void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, int statusCode, String error) throws IOException {
        String jsonResponse = gson.toJson(Map.of("error", error));
        sendResponse(exchange, jsonResponse, statusCode);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendError(exchange, 404, "Объект не найден");
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendError(exchange, 400, "Неверный формат запроса: " + message);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendError(exchange, 406, "Задача пересекается с существующими");
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        sendError(exchange, 500, "Внутренняя ошибка сервера");
    }

    protected int parsePathId(String path) {
        try {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            String[] parts = path.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new BadRequestFormatException("Некорректный формат ID в пути: " + path);
        }
    }

    protected String normalizePath(String path) {
        if (path.endsWith("/") && path.length() > 1) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
package com.yandex.kanban.server;

import com.yandex.kanban.service.TaskManager;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String json = gson.toJson(manager.getHistory());
            sendResponse(exchange, json, 200);
        } else {
            sendError(exchange, 405, "Метод не поддерживается");
        }
    }
}
package com.yandex.kanban.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.kanban.model.*;
import com.yandex.kanban.server.HttpTaskServer;
import com.yandex.kanban.service.InMemoryTaskManager;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.util.DurationAdapter;
import com.yandex.kanban.util.LocalDateTimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private final HttpClient client = HttpClient.newHttpClient();
    private int epicId;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        taskServer.start();

        Epic epic = new Epic("Epic for Subtasks", "Test Epic");
        epicId = manager.createEpic(epic);
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(
                "Test Subtask",
                "Test Description",
                Status.NEW,
                epicId,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    void testGetSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(
                "Test Subtask",
                "Test Description",
                Status.NEW,
                epicId,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );
        int subtaskId = manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(
                "Test Subtask",
                "Test Description",
                Status.NEW,
                epicId,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );
        int subtaskId = manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}
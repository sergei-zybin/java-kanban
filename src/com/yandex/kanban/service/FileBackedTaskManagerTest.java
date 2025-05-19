package com.yandex.kanban.service;

import com.yandex.kanban.model.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    @Test
    public void testSaveAndLoadFromFile() {
        File tempFile = null;
        try {
            File tempDir = new File(System.getProperty("user.home"), "kanban-temp");
            tempDir.mkdirs();
            tempFile = File.createTempFile("tasks", ".csv", tempDir);

            TaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);

            Epic epic = new Epic("Epic", "Test epic");
            int epicId = manager.createEpic(epic);

            Subtask subtask = new Subtask("Subtask", "Test subtask", Status.NEW, epicId);
            manager.createSubtask(subtask);

            Task task = new Task("Task", "Test task", Status.IN_PROGRESS);
            manager.createTask(task);

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            assertEquals(1, loadedManager.getAllTasks().size());
            assertEquals(1, loadedManager.getAllEpics().size());

        } catch (IOException e) {
            fail("Ошибка: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}
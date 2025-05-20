package com.yandex.kanban.test;

import com.yandex.kanban.exception.TimeConflictException;
import com.yandex.kanban.model.*;
import com.yandex.kanban.service.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final String TEST_DIR = System.getProperty("user.home") + "/kanban-test";
    private File testFile;

    @BeforeEach
    void setup() throws IOException {
        Files.createDirectories(Path.of(TEST_DIR));
    }

    @AfterEach
    void cleanup() throws IOException {
        if (testFile != null && testFile.exists()) {
            Files.deleteIfExists(testFile.toPath());
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        testFile = new File(TEST_DIR, "tasks_" + System.currentTimeMillis() + ".csv");
        return new FileBackedTaskManager(testFile);
    }

    @Test
    void testSaveAndLoadWithFullData() {
        FileBackedTaskManager manager1 = createTaskManager();

        Epic epic = new Epic("Epic", "Test epic");
        int epicId = manager1.createEpic(epic);

        LocalDateTime subtaskTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Subtask subtask = new Subtask("Subtask", "Test", Status.NEW, epicId,
                subtaskTime, Duration.ofMinutes(30));
        int subtaskId = manager1.createSubtask(subtask);

        LocalDateTime taskTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        Task task = new Task("Task", "Test", Status.IN_PROGRESS,
                taskTime, Duration.ofHours(2));
        int taskId = manager1.createTask(task);

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(testFile);

        assertAll(
                () -> assertEquals(1, manager2.getAllTasks().size()),
                () -> assertEquals(1, manager2.getAllEpics().size()),
                () -> assertEquals(1, manager2.getAllSubtasks().size()),
                () -> assertEquals(taskTime, manager2.getTask(taskId).orElseThrow().getStartTime()),
                () -> assertEquals(subtaskTime, manager2.getEpic(epicId).orElseThrow().getStartTime())
        );
    }

    @Test
    void testLoadFromNonExistentFile() {
        File invalidFile = new File(TEST_DIR, "non_existent.csv");
        assertThrows(
                FileBackedTaskManager.ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(invalidFile)
        );
    }

    @Test
    void shouldDetectTimeConflictsWhenLoading() throws IOException {
        FileBackedTaskManager manager = createTaskManager();

        LocalDateTime time = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW, time, Duration.ofHours(1));
        Task task2 = new Task("Task2", "Desc", Status.NEW, time.plusMinutes(30), Duration.ofHours(1));

        manager.createTask(task1);

        assertThrows(
                TimeConflictException.class,
                () -> manager.createTask(task2)
        );
    }

    @Test
    public void testSaveAndLoadFromFile() {
        File tempFile = null;
        try {
            File tempDir = new File(System.getProperty("user.home"), "kanban-temp");
            tempDir.mkdirs();
            tempFile = File.createTempFile("tasks", ".csv", tempDir);

            FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile);

            Epic epic = new Epic("Epic", "Test epic");
            int epicId = manager1.createEpic(epic);

            LocalDateTime subtaskStartTime = LocalDateTime.of(2023, 1, 1, 10, 0);
            Duration subtaskDuration = Duration.ofMinutes(30);
            Subtask subtask = new Subtask("Subtask", "Test subtask", Status.NEW, epicId,
                    subtaskStartTime, subtaskDuration);
            int subtaskId = manager1.createSubtask(subtask);

            LocalDateTime taskStartTime = LocalDateTime.of(2023, 1, 1, 12, 0);
            Duration taskDuration = Duration.ofHours(2);
            Task task = new Task("Task", "Test task", Status.IN_PROGRESS,
                    taskStartTime, taskDuration);
            int taskId = manager1.createTask(task);

            Optional<Epic> savedEpic = manager1.getEpic(epicId);
            assertTrue(savedEpic.isPresent());
            ;
            assertEquals(subtaskStartTime, savedEpic.get().getStartTime());
            assertEquals(subtaskDuration, savedEpic.get().getDuration());
            assertEquals(subtaskStartTime.plus(subtaskDuration), savedEpic.get().getEndTime());

            FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(tempFile);

            Optional<Task> loadedTask = manager2.getTask(taskId);
            assertNotNull(loadedTask);
            assertEquals(taskStartTime, loadedTask.get().getStartTime());
            assertEquals(taskDuration, loadedTask.get().getDuration());
            assertEquals(taskStartTime.plus(taskDuration), loadedTask.get().getEndTime());

            Optional<Subtask> loadedSubtask = manager2.getSubtask(subtaskId);
            assertNotNull(loadedSubtask);
            assertEquals(subtaskStartTime, loadedSubtask.get().getStartTime());
            assertEquals(subtaskDuration, loadedSubtask.get().getDuration());

            Optional<Epic> loadedEpic = manager2.getEpic(epicId);
            assertNotNull(loadedEpic);

            assertEquals(subtaskStartTime, loadedEpic.get().getStartTime());
            assertEquals(subtaskDuration, loadedEpic.get().getDuration());
            assertEquals(subtaskStartTime.plus(subtaskDuration), loadedEpic.get().getEndTime());

            assertEquals(1, loadedEpic.get().getSubtaskIds().size());
            assertTrue(loadedEpic.get().getSubtaskIds().contains(subtaskId));

        } catch (IOException e) {
            fail("Ошибка во время теста: " + e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
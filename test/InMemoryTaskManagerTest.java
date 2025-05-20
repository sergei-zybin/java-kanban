package com.yandex.kanban.test;

import com.yandex.kanban.exception.TimeConflictException;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Status;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void updateTask() {

        Task task = new Task("Task", "Desc", Status.NEW);
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task("Updated", "New Desc", Status.DONE);
        updatedTask.setId(taskId);

        taskManager.updateTask(updatedTask);

        Optional<Task> savedTask = taskManager.getTask(taskId);
        assertTrue(savedTask.isPresent());
        assertEquals(Status.DONE, savedTask.get().getStatus(), "Статус не обновлён");
    }

    @Test
    void checkTimeConflict() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW,
                LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(30));

        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task2));
    }

    @Test
    void checkMultipleTimeConflicts() {
        LocalDateTime baseTime = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                baseTime, Duration.ofMinutes(30));
        taskManager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW,
                baseTime.minusMinutes(15), Duration.ofMinutes(30));
        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task2));

        Task task3 = new Task("Task3", "Desc", Status.NEW,
                baseTime.plusMinutes(15), Duration.ofMinutes(30));
        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task3));

        Task task4 = new Task("Task4", "Desc", Status.NEW,
                baseTime.minusMinutes(15), Duration.ofHours(1));
        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task4));
    }

    @Test
    void subtaskShouldHaveValidEpicLink() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epicId, null, null);
        int subtaskId = taskManager.createSubtask(subtask);

        Optional<Subtask> savedSubtask = taskManager.getSubtask(subtaskId);
        assertTrue(savedSubtask.isPresent());
        assertEquals(epicId, savedSubtask.get().getEpicId(),
                "Подзадача должна быть связана с правильным эпиком");
    }

}
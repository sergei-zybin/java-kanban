package com.yandex.kanban.test;

import com.yandex.kanban.model.*;
import com.yandex.kanban.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void createAndGetTask() {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = taskManager.createTask(task);
        Optional<Task> savedTask = taskManager.getTask(taskId);
        assertTrue(savedTask.isPresent(), "Задача не найдена");
        assertEquals(task.getName(), savedTask.get().getName(), "Названия задач не совпадают");
    }

    @Test
    void epicStatusAllNew() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.createEpic(epic);
        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId, null, null);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW, epicId, null, null);
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(Status.NEW, taskManager.getEpic(epicId).get().getStatus());
    }

    @Test
    void epicStatusAllDone() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.createEpic(epic);
        Subtask sub1 = new Subtask("Sub1", "Desc", Status.DONE, epicId, null, null);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.DONE, epicId, null, null);
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        assertEquals(Status.DONE, taskManager.getEpic(epicId).get().getStatus());
    }

    @Test
    void epicStatusMixedNewAndDone() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId, null, null);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.DONE, epicId, null, null);
        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).get().getStatus(),
                "Смешанные статусы NEW и DONE должны давать IN_PROGRESS");
    }

    @Test
    void epicStatusInProgress() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.IN_PROGRESS, epicId, null, null);
        taskManager.createSubtask(sub1);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpic(epicId).get().getStatus(),
                "Подзадачи IN_PROGRESS должны давать статус IN_PROGRESS");
    }
}
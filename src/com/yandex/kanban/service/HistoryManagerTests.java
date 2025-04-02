package com.yandex.kanban.service;

import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.Status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTests {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        TaskManager manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        manager.clearAll();

    }

    @Test
    void historyPreservesOriginalTaskDataAfterModification() {

        Task originalTask = new Task("Task 1", "Description 1", Status.NEW);
        originalTask.setId(1);
        historyManager.addTask(originalTask);

        originalTask.setName("Modified Name");
        originalTask.setDescription("Modified Description");
        originalTask.setStatus(Status.IN_PROGRESS);

        Task historyTask = historyManager.getHistory().getFirst();

        assertEquals(1, historyTask.getId(), "ID задачи должен совпадать");
        assertEquals("Task 1", historyTask.getName(), "Название не должно измениться");
        assertEquals("Description 1", historyTask.getDescription(), "Описание не должно измениться");
        assertEquals(Status.NEW, historyTask.getStatus(), "Статус должен остаться исходным");
    }

    @Test
    void shouldKeepOnlyLast10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Desc", Status.NEW);
            task.setId(i);
            historyManager.addTask(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна содержать 10 задач");
        assertEquals(6, history.getFirst().getId(), "Первая задача должна быть с ID=6");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Desc", Status.NEW);
        task.setId(1);
        historyManager.addTask(task);

        historyManager.remove(1);

        if (!historyManager.getHistory().isEmpty()) {
            throw new AssertionError("История должна быть пустой после удаления");
        }
    }
}






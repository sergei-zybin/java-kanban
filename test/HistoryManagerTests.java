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
    void historyHasNoDuplicates() {
        Task task = new Task("Task", "Desc", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну запись");
        assertEquals(task.getId(), history.getFirst().getId());
    }

    @Test
    void removeTaskFromMiddleOfHistory() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertEquals(1, history.getFirst().getId()),
                () -> assertEquals(3, history.get(1).getId())
        );
    }

    @Test
    void historyPreservesOriginalTaskDataAfterModification() {

        Task originalTask = new Task("Task 1", "Description 1", Status.NEW);
        originalTask.setId(1);
        historyManager.add(originalTask);

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
    void shouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Desc", Status.NEW);
        task.setId(1);
        historyManager.add(task);

        historyManager.remove(1);

        if (!historyManager.getHistory().isEmpty()) {
            throw new AssertionError("История должна быть пустой после удаления");
        }
    }

}




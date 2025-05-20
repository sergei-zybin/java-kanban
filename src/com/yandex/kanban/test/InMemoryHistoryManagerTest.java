package com.yandex.kanban.test;

import com.yandex.kanban.model.Status;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.HistoryManager;
import com.yandex.kanban.service.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setId(2);
    }

    @Test
    void addAndRetrieveHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    void removeFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        assertFalse(historyManager.getHistory().contains(task1));
    }

    @Test
    void handleDuplicates() {
        historyManager.add(task1);
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void emptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "История должна быть пустой при инициализации");
    }

    @Test
    void removeFromMiddle() {
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertFalse(history.contains(task2)),
                () -> assertEquals(List.of(task1, task3), history)
        );
    }

    @Test
    void removeFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertAll(
                () -> assertEquals(1, history.size()),
                () -> assertEquals(task1, history.get(0))
        );
    }
}
package com.yandex.kanban.service;

import com.yandex.kanban.model.Status;
import com.yandex.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTests {
    private HistoryManager history;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setup() {
        history = new InMemoryHistoryManager();
        task1 = new Task("Проверка истории", "Описание 1", Status.NEW);
        task2 = new Task("Еще задача", "Описание 2", Status.DONE);
        task1.setId(1);
        task2.setId(2);
    }

    @Test
    void addTaskShouldUpdateHistoryOrder() {
        history.add(task1);
        history.add(task2);
        history.add(task1);
        assertEquals(List.of(2, 1), getHistoryIds(),
                "История должна обновлять порядок при повторном просмотре");
    }

    @Test
    void removeTaskShouldWorkFromAllPositions() {
        Task task3 = new Task("Задача 3", "Описание", Status.IN_PROGRESS);
        task3.setId(3);
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(2);
        assertEquals(List.of(1, 3), getHistoryIds());

        history.remove(1);
        assertEquals(List.of(3), getHistoryIds());

        history.remove(3);
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    void taskInHistoryShouldBeImmutable() {
        history.add(task1);
        task1.setStatus(Status.DONE);

        assertEquals(Status.NEW,
                history.getHistory().getFirst().getStatus(),
                "История должна хранить неизменяемые копии задач");
    }

    private List<Integer> getHistoryIds() {
        return history.getHistory().stream()
                .map(Task::getId)
                .toList();
    }
}





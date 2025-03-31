package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class HistoryManagerTests {
    private TaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        historyManager.clear();
        manager.clearAll();
    }

    @Test
    void historyPreservesOriginalTaskState() {
        Task task = new Task("Task", "Desc", Status.NEW);
        manager.createTask(task);
        historyManager.add(task);

        task.setStatus(Status.DONE);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(Status.NEW, history.get(0).getStatus(), "Первая копия должна быть NEW");
        assertEquals(Status.DONE, history.get(1).getStatus(), "Вторая копия должна быть DONE");
    }

    @Test
    void historyManagerComplexBehavior() {
        manager.clearAll();
        historyManager.clear();

        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Desc", Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "История должна хранить только последние 10 задач");
        assertEquals(6, history.get(0).getId(), "Первая задача в истории должна быть с ID=6");

        Task task = history.get(5);
        historyManager.add(task);
        history = historyManager.getHistory();
        assertEquals(10, history.size(), "Размер истории не должен измениться");
        assertEquals(task.getId(), history.get(9).getId(), "Задача должна переместиться в конец");

        int idToRemove = history.get(3).getId();
        manager.deleteTask(idToRemove);
        history = historyManager.getHistory();
        assertFalse(history.stream().anyMatch(t -> t.getId() == idToRemove),
                "Задача должна исчезнуть из истории");

        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);
        Subtask subtask = new Subtask("Task", "Desc", Status.DONE, epicId);
        int subId = manager.createSubtask(subtask);

        Epic savedEpic = manager.getEpic(epicId);
        Subtask savedSubtask = manager.getSubtask(subId);

        historyManager.add(savedEpic);
        historyManager.add(savedSubtask);
        history = historyManager.getHistory();

        Task lastTask = history.getLast();
        Task secondLastTask = history.get(history.size() - 2);

        assertInstanceOf(Epic.class, secondLastTask, "Элемент должен быть Epic");
        assertEquals(1, secondLastTask.getId(), "ID эпика должно быть 1");

        assertInstanceOf(Subtask.class, lastTask, "Элемент должен быть Subtask");
        assertEquals(2, lastTask.getId(), "ID подзадачи должно быть 2");

        subtask.setStatus(Status.NEW);
        historyManager.add(subtask);
        Task historySubtaskAfterChange = historyManager.getHistory().getLast();

        assertEquals(Status.NEW, historySubtaskAfterChange.getStatus(),
                "История должна хранить актуальные копии");
    }
}
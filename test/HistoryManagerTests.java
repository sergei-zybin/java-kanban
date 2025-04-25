package com.yandex.kanban.service;

import com.yandex.kanban.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List; 
import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTests {
    private TaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
        manager.clearAll();
        for (Task task : historyManager.getHistory()) {
            historyManager.remove(task.getId());
        }
    }

    @Test
    void historyHasNoDuplicates() {
        Task task = new Task("Task", "Desc", Status.NEW);
        int taskId = manager.createTask(task);

        manager.getTask(taskId);
        manager.getTask(taskId);
        manager.getTask(taskId);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История содержит дубликаты");
        assertEquals(taskId, history.get(0).getId());
    }

    @Test
    void removeTaskFromMiddleOfHistory() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        int task1Id = manager.createTask(task1);
        Task task2 = new Task("Task2", "Desc", Status.NEW);
        int task2Id = manager.createTask(task2);
        Task task3 = new Task("Task3", "Desc", Status.NEW);
        int task3Id = manager.createTask(task3);

        manager.getTask(task1Id);
        manager.getTask(task2Id);
        manager.getTask(task3Id);

        historyManager.remove(task2Id);

        List<Task> history = manager.getHistory();
        assertAll(
                () -> assertEquals(2, history.size(), "Неверный размер истории"),
                () -> assertEquals(task1Id, history.get(0).getId(), "Первая задача не совпадает"),
                () -> assertEquals(task3Id, history.get(1).getId(), "Третья задача не на месте")
        );
    }

    @Test
    void historyPreservesOriginalTaskDataAfterModification() {

    Task original = new Task("Original", "Desc", Status.NEW);
    int taskId = manager.createTask(original);
    
    Task savedTask = manager.getTask(taskId);
        
    savedTask.setName("Modified");
    savedTask.setDescription("New Desc");
    savedTask.setStatus(Status.DONE);
    
    Task historyTask = manager.getHistory().get(0);
      
    assertAll(
        () -> assertEquals("Original", historyTask.getName(), "Название изменилось в истории!"),
        () -> assertEquals("Desc", historyTask.getDescription(), "Описание изменилось в истории!"),
        () -> assertEquals(Status.NEW, historyTask.getStatus(), "Статус изменился в истории!")
    );
}


    @Test
    void shouldRemoveTaskFromHistory() {
        Task task = new Task("Task", "Desc", Status.NEW);
        int taskId = manager.createTask(task);

        manager.getTask(taskId); 
        historyManager.remove(taskId);

        assertTrue(manager.getHistory().isEmpty(), "История не пуста после удаления");
    }
}



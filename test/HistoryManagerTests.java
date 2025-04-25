package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Status;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTests {
    private TaskManager manager;
    private Epic testEpic;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
        testEpic = new Epic("Тестовый эпик", "Описание");
        int epicId = manager.createEpic(testEpic);
        testEpic.setId(epicId);
    }

    @Test
    void shouldHandleTaskConflicts() {
        Task task = new Task("Конфликт ID", "Описание", Status.NEW);
        task.setId(1); 

        assertThrows(
            IllegalArgumentException.class,
            () -> manager.createTask(task),
            "Должна быть ошибка при конфликте ID"
        );
    }

    @Test
    void epicShouldUpdateStatusAutomatically() {
        Subtask subtask = new Subtask("Подзадача", "Описание", Status.DONE, testEpic.getId());
        manager.createSubtask(subtask);

        assertEquals(
            Status.DONE,
            manager.getEpic(testEpic.getId()).getStatus(),
            "Статус эпика должен обновляться автоматически"
        );
    }

    @Test
    void deletingEpicShouldRemoveSubtasks() {
        Subtask subtask = new Subtask("Удаляемая подзадача", "Описание", Status.NEW, testEpic.getId());
        int subtaskId = manager.createSubtask(subtask);

        manager.deleteEpic(testEpic.getId());
        assertNull(
            manager.getSubtask(subtaskId),
            "Подзадачи должны удаляться вместе с эпиком"
        );
    }

    @Test
    void historyShouldNotContainDeletedTasks() {
        Task task = new Task("Временная задача", "Описание", Status.NEW);
        int taskId = manager.createTask(task);

        manager.getTask(taskId); 
        manager.deleteTask(taskId);

        List<Task> history = manager.getHistory();
        assertFalse(
            history.stream().anyMatch(t -> t.getId() == taskId),
            "Удаленные задачи должны исчезать из истории"
        );
    }
}




package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTests {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void taskEqualityById() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc2", Status.DONE);
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void subclassEqualityById() {
        Epic epic1 = new Epic("Epic1", "Desc");
        epic1.setId(1);
        Epic epic2 = new Epic("Epic2", "Desc2");
        epic2.setId(1);

        Subtask subtask1 = new Subtask("Sub1", "Desc", Status.NEW, 1);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Sub2", "Desc2", Status.DONE, 1);
        subtask2.setId(2);

        assertAll(
                () -> assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны"),
                () -> assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны")
        );
    }

    @Test
    void epicCantBeSubtaskToItself() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epicId);
        subtask.setId(epicId);

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Эпик не может быть своей же подзадачей"
        );
    }

    @Test
    void subtaskCantBeItsOwnEpic() {
        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, 1);
        subtask.setId(1);

        assertThrows(IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Подзадача не может быть своим эпиком"
        );
    }

    @Test
    void managersAreInitialized() {
        assertAll(
                () -> assertNotNull(Managers.getDefault(), "Менеджер задач должен быть проинициализирован"),
                () -> assertNotNull(Managers.getDefaultHistory(), "Менеджер истории должен быть проинициализирован")
        );
    }

    @Test
    void addAndFindDifferentTaskTypes() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);
        Epic savedEpic = manager.getEpic(epicId);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epicId);
        int subId = manager.createSubtask(subtask);
        Subtask savedSubtask = manager.getSubtask(subId);

        Task task = new Task("Task", "Desc", Status.NEW);
        int taskId = manager.createTask(task);
        Task savedTask = manager.getTask(taskId);

        assertAll(
                () -> assertEquals(epic.getName(), savedEpic.getName()),
                () -> assertEquals(epic.getDescription(), savedEpic.getDescription()),
                () -> assertEquals(subtask.getEpicId(), savedSubtask.getEpicId()),
                () -> assertEquals(task.getStatus(), savedTask.getStatus())
        );
    }

    @Test
    void manualAndGeneratedIdsDontConflict() {
        Task taskWithManualId = new Task("Task1", "Desc", Status.NEW);
        taskWithManualId.setId(100);
        manager.createTask(taskWithManualId);

        Task taskWithGeneratedId = new Task("Task2", "Desc", Status.NEW);
        int generatedId = manager.createTask(taskWithGeneratedId);

        assertNotEquals(100, generatedId, "ID не должны конфликтовать");
        assertNotNull(manager.getTask(100));
        assertNotNull(manager.getTask(generatedId));
    }

    @Test
    void taskImmutabilityWhenAdded() {
        Task original = new Task("Original", "Desc", Status.NEW);
        int id = manager.createTask(original);

        original.setName("Modified");
        original.setStatus(Status.DONE);

        Task fromManager = manager.getTask(id);
        assertAll(
                () -> assertEquals("Original", fromManager.getName()),
                () -> assertEquals(Status.NEW, fromManager.getStatus())
        );
    }

    @Test
    void epicStatusUpdatesBasedOnSubtasksComplexScenario() {
        Epic epic = new Epic("Complex Epic", "Epic Desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", Status.NEW, epicId);
        Subtask subtask3 = new Subtask("Subtask 3", "Desc", Status.NEW, epicId);

        int id1 = manager.createSubtask(subtask1);
        int id2 = manager.createSubtask(subtask2);
        int id3 = manager.createSubtask(subtask3);

        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus(), "Эпик должен быть NEW");

        Subtask updatedSubtask1 = new Subtask("Subtask 1", "Desc", Status.IN_PROGRESS, epicId);
        updatedSubtask1.setId(id1);
        manager.updateSubtask(updatedSubtask1);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Эпик должен перейти в IN_PROGRESS");

        Subtask updatedSubtask2 = new Subtask("Subtask 2", "Desc", Status.DONE, epicId);
        updatedSubtask2.setId(id2);
        manager.updateSubtask(updatedSubtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Эпик остается IN_PROGRESS при смешанных статусах");

        manager.deleteSubtask(id3);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Эпик остается IN_PROGRESS после удаления подзадачи");

        updatedSubtask1.setStatus(Status.DONE);
        manager.updateSubtask(updatedSubtask1);
        manager.updateSubtask(updatedSubtask2);

        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus(),
                "Эпик должен перейти в DONE, когда все подзадачи завершены");

        Subtask subtask4 = new Subtask("Subtask 4", "Desc", Status.IN_PROGRESS, epicId);
        manager.createSubtask(subtask4);

        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(),
                "Эпик должен вернуться в IN_PROGRESS при добавлении новой активной подзадачи");
    }

    @Test
    void epicShouldNotHaveRemovedSubtaskIds() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epicId);
        int subId = manager.createSubtask(subtask);

        manager.deleteSubtask(subId);
        Epic savedEpic = manager.getEpic(epicId);

        assertFalse(savedEpic.getSubtaskIds().contains(subId), "Эпик не должен содержать удалённую подзадачу");
    }

    @Test
    void deletingEpicRemovesAllSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", Status.NEW, epicId);
        int subId1 = manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub2", "Desc", Status.NEW, epicId);
        int subId2 = manager.createSubtask(subtask2);

        manager.deleteEpic(epicId);

        assertNull(manager.getSubtask(subId1), "Подзадача должна быть удалена");
        assertNull(manager.getSubtask(subId2), "Подзадача должна быть удалена");
    }

    @Test
    void subtaskEpicIdCannotBeChangedExternally() {
        Epic epic1 = new Epic("Epic1", "Desc");
        int epic1Id = manager.createEpic(epic1);

        Epic epic2 = new Epic("Epic2", "Desc");
        int epic2Id = manager.createEpic(epic2);

        Subtask subtask = new Subtask("Sub", "Desc", Status.NEW, epic1Id);
        int subId = manager.createSubtask(subtask);

        Subtask fromManager = manager.getSubtask(subId);

        Subtask updatedSubtask = manager.getSubtask(subId);
        assertEquals(epic1Id, updatedSubtask.getEpicId(),
                "epicId не должен изменяться внешне");
    }
}
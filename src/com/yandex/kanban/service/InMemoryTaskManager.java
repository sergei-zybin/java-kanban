package com.yandex.kanban.service;

import com.yandex.kanban.model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTask(int id) { // Убрал избыточную проверку null
        Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public int createTask(Task task) {
        Task copy = task.copy();

        if (copy.getId() != 0) {
            if (tasks.containsKey(copy.getId())) {
                throw new IllegalArgumentException("Конфликт ID");
            }
            nextId = Math.max(nextId, copy.getId() + 1);
        } else {
            copy.setId(nextId++);
        }

        tasks.put(copy.getId(), copy);
        return copy.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task copy = task.copy();
            tasks.put(copy.getId(), copy);
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        Subtask copy = subtask.copy();

        if (copy.getId() == copy.getEpicId()) {
            throw new IllegalArgumentException("Подзадача не может быть своим эпиком!");
        }

        Epic epic = epics.get(copy.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + copy.getEpicId() + " не найден!");
        }

        if (copy.getId() != 0) {
            if (subtasks.containsKey(copy.getId()) || tasks.containsKey(copy.getId())) {
                throw new IllegalArgumentException("ID " + copy.getId() + " уже занят!");
            }
            nextId = Math.max(nextId, copy.getId() + 1);
        } else {
            copy.setId(nextId++);
        }

        subtasks.put(copy.getId(), copy);
        epic.addSubtask(copy.getId());
        updateEpicStatus(epic);

        return copy.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask copy = subtask.copy();
            subtasks.put(copy.getId(), copy);
            updateEpicStatus(epics.get(copy.getEpicId()));
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtask(id);
            updateEpicStatus(epic);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public int createEpic(Epic epic) {
        Epic copy = epic.copy();
        copy.setId(nextId++);
        epics.put(copy.getId(), copy);
        return copy.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();

        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            result.add(subtasks.get(subtaskId));
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void clearAll() {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        nextId = 1;
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Status.NEW) allNew = false;
            if (subtask.getStatus() != Status.DONE) allDone = false;
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}


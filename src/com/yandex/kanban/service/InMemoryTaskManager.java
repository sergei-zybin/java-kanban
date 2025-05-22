package com.yandex.kanban.service;

import com.yandex.kanban.exception.TimeConflictException;
import com.yandex.kanban.model.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task); // Добавил удаление из prioritizedTasks
        }
        tasks.clear();
    }

    @Override
    public Optional<Task> getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public int createTask(Task task) {
        if (hasAnyOverlap(task)) {
            throw new TimeConflictException("Задача пересекается с существующими");
        }

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
        if (copy.getStartTime() != null) {
            prioritizedTasks.add(copy);
        }
        return copy.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (hasAnyOverlap(task)) {
            throw new TimeConflictException("Задача пересекается с существующими");
        }
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask);

            Task copy = task.copy();
            tasks.put(copy.getId(), copy);

            if (copy.getStartTime() != null) {
                prioritizedTasks.add(copy);
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask); // Добавил удаление из prioritizedTasks
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        if (hasAnyOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается с существующими");
        }
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
        if (copy.getStartTime() != null) {
            prioritizedTasks.add(copy);
        }
        epic.addSubtask(copy.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);

        return copy.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasAnyOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается с существующими");
        }
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            prioritizedTasks.remove(oldSubtask);

            Subtask copy = subtask.copy();
            subtasks.put(copy.getId(), copy);

            if (copy.getStartTime() != null) {
                prioritizedTasks.add(copy);
            }
            Epic epic = epics.get(copy.getEpicId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtask(id);
            updateEpicStatus(epic);
            updateEpicTime(epic);
            historyManager.remove(id);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask); // Добавил удаление из prioritizedTasks
        }
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return Optional.ofNullable(epic);
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
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask); // Добавил удаление из prioritizedTasks
                    historyManager.remove(subtaskId);
                }
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtaskIds)
                .orElse(Collections.emptyList())
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subtasksList = getEpicSubtasks(epic.getId());

        if (subtasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = subtasksList.stream()
                .allMatch(s -> s.getStatus() == Status.NEW);

        boolean allDone = subtasksList.stream()
                .allMatch(s -> s.getStatus() == Status.DONE);

        epic.setStatus(
                allNew ? Status.NEW :
                        allDone ? Status.DONE :
                                Status.IN_PROGRESS
        );
    }

    private void updateEpicTime(Epic epic) {
        List<Subtask> subtasksList = getEpicSubtasks(epic.getId());

        if (subtasksList.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        Optional<LocalDateTime> earliestStart = subtasksList.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        Optional<LocalDateTime> latestEnd = subtasksList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        Duration totalDuration = subtasksList.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(earliestStart.orElse(null));
        epic.setEndTime(latestEnd.orElse(null));
        epic.setDuration(totalDuration);
    }

    protected void updateAllEpicsTime() {
        epics.values().forEach(this::updateEpicTime);
    }

    private boolean hasTimeOverlap(Task a, Task b) {
        if (a.getStartTime() == null || a.getDuration() == null ||
                b.getStartTime() == null || b.getDuration() == null) {
            return false;
        }

        LocalDateTime aStart = a.getStartTime();
        LocalDateTime aEnd = a.getEndTime();
        LocalDateTime bStart = b.getStartTime();
        LocalDateTime bEnd = b.getEndTime();

        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private boolean hasAnyOverlap(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> t.getStartTime() != null)
                .anyMatch(t -> hasTimeOverlap(task, t));
    }

}
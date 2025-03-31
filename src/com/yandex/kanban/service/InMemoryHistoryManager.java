package com.yandex.kanban.service;
import com.yandex.kanban.model.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;
        task.incrementViews();
        Task copy = deepCopy(task);
        history.addLast(copy);

        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    private Task deepCopy(Task original) {
        if (original instanceof Epic epic) {
            Epic copy = new Epic(epic.getName(), epic.getDescription());
            copy.setId(epic.getId());
            copy.setStatus(epic.getStatus());
            copy.setViews(epic.getViews());
            epic.getSubtaskIds().forEach(copy::addSubtask);
            return copy;
        } else if (original instanceof Subtask subtask) {
            Subtask copy = new Subtask(
                    subtask.getName(),
                    subtask.getDescription(),
                    subtask.getStatus(),
                    subtask.getEpicId()
            );
            copy.setId(subtask.getId());
            copy.setViews(subtask.getViews());
            return copy;
        } else {
            Task copy = new Task(
                    original.getName(),
                    original.getDescription(),
                    original.getStatus()
            );
            copy.setId(original.getId());
            copy.setViews(original.getViews());
            return copy;
        }
    }

    @Override
    public void clear() {
        history.clear();
    }

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
    }

}
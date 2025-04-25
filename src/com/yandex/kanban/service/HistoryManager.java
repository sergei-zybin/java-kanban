package com.yandex.kanban.service;

import com.yandex.kanban.model.Task;
import java.util.List;

public interface HistoryManager {
    void addTask(Task task);
    void remove(int id); // clear() убрал, remove() оставил
    List<Task> getHistory();
}
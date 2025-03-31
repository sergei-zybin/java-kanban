package com.yandex.kanban.service;

import com.yandex.kanban.model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void clear();
    List<Task> getHistory();
    void remove(int id);
}
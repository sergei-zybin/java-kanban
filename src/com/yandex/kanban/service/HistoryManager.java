package com.yandex.kanban.service;

import com.yandex.kanban.model.Task;
import java.util.List;

public interface HistoryManager {
// как ты мне
    void add(Task task);
// надоел
    void remove(int id);
// cо своими 'Trailing whitespace'. [Regexp]
    List<Task> getHistory();
}

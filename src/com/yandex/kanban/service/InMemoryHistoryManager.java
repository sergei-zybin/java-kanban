package com.yandex.kanban.service;
import com.yandex.kanban.model.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new LinkedList<>(); // Исправил

    @Override
    public void addTask(Task task) { // Исправил скобки и условие вместо цикла
        if (task == null) {
            return;
        }
        if (history.size() == MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        history.addLast(task.copy());
    }

    @Override
    public List<Task> getHistory() { // Историю с deepCopy убрал
        return new ArrayList<>(history);
    }

    // Убрал incrementViews() - с его помощью для самопроверки отображал кол-во просмотров/обращений.
    // Это было для меню в Main. Но пока, действительно, лишнее.

    @Override
    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
    }
}
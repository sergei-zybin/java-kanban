package com.yandex.kanban.service;

public class Managers {
    private static final HistoryManager DEFAULT_HISTORY_MANAGER = new InMemoryHistoryManager();
    private static final TaskManager DEFAULT_TASK_MANAGER = new InMemoryTaskManager();

    public static TaskManager getDefault() {
        return DEFAULT_TASK_MANAGER;
    }

    public static HistoryManager getDefaultHistory() {
        return DEFAULT_HISTORY_MANAGER;
    }
}
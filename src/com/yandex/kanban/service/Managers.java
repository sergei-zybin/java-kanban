package com.yandex.kanban.service;

import java.io.File;

public final class Managers {

    private Managers() {
        throw new UnsupportedOperationException("Утилитарный класс");
    }

    private static final HistoryManager DEFAULT_HISTORY_MANAGER = new InMemoryHistoryManager();
    private static final TaskManager DEFAULT_TASK_MANAGER = new InMemoryTaskManager();

    public static TaskManager getDefault() {
        return DEFAULT_TASK_MANAGER;
    }

    public static TaskManager getDefault(String path) {
        return new FileBackedTaskManager(new File(path));
    }

    public static HistoryManager getDefaultHistory() {
        return DEFAULT_HISTORY_MANAGER;
    }
}
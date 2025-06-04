package com.yandex.kanban.service;

import java.io.File;

public final class Managers {

    private Managers() {
        throw new UnsupportedOperationException("Утилитарный класс");
    }

    private static final HistoryManager DEFAULT_HISTORY_MANAGER = new InMemoryHistoryManager();
    private static final TaskManager DEFAULT_TASK_MANAGER = new InMemoryTaskManager();
    private static final TaskManager FILE_BACKED_TASK_MANAGER = new FileBackedTaskManager(new File("tasks.csv"));

    public static TaskManager getDefault() {
        return DEFAULT_TASK_MANAGER;
    }

    public static TaskManager getFileBacked() {
        return FILE_BACKED_TASK_MANAGER;
    }

    public static HistoryManager getDefaultHistory() {
        return DEFAULT_HISTORY_MANAGER;
    }
}
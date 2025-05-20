package com.yandex.kanban.service;

import com.yandex.kanban.model.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,startTime,duration\n");

            for (Task task : tasks.values()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : epics.values()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        Integer id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    private String toString(Task task) {
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";

        if (task instanceof Epic) {
            return String.format("%d,%s,%s,%s,%s,,%s,%s",
                    task.getId(), TaskType.EPIC, task.getName(), task.getStatus(), task.getDescription(),
                    startTimeStr, durationStr);
        } else if (task instanceof Subtask subtask) {
            return String.format("%d,%s,%s,%s,%s,%d,%s,%s",
                    subtask.getId(), TaskType.SUBTASK, subtask.getName(), subtask.getStatus(),
                    subtask.getDescription(), subtask.getEpicId(),
                    startTimeStr, durationStr);
        } else {
            return String.format("%d,%s,%s,%s,%s,,%s,%s",
                    task.getId(), TaskType.TASK, task.getName(), task.getStatus(), task.getDescription(),
                    startTimeStr, durationStr);
        }
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        LocalDateTime startTime = parts.length > 6 && !parts[6].isEmpty() ?
                LocalDateTime.parse(parts[6]) : null;
        Duration duration = parts.length > 7 && !parts[7].isEmpty() ?
                Duration.ofMinutes(Long.parseLong(parts[7])) : null;

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, status, epicId, startTime, duration);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                    Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                    if (epic != null) {
                        epic.addSubtask(task.getId());
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
                if (manager.nextId <= task.getId()) {
                    manager.nextId = task.getId() + 1;
                }
            }
            manager.updateAllEpicsTime();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
        return manager;
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    int createTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);


    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    Subtask getSubtask(int id);

    Integer createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);


    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);


    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();

    void clearAll();
}

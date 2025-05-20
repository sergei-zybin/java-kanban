package com.yandex.kanban;

import com.yandex.kanban.model.*;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.service.Managers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault("tasks.csv");

        System.out.println("+++ Создание объектов +++");
        Task foundation = new Task("Закладка фундамента", "Бетонная заливка", Status.IN_PROGRESS,
                LocalDateTime.of(2023, 1, 1, 9, 0), Duration.ofHours(8));
        int task1Id = manager.createTask(foundation);

        Task walls = new Task("Установка стен", "Каркасные стены", Status.NEW,
                LocalDateTime.of(2023, 1, 2, 9, 0), Duration.ofDays(2));
        int task2Id = manager.createTask(walls);

        Epic roof = new Epic("Строительство крыши", "Полная конструкция");
        int epic1Id = manager.createEpic(roof);

        Subtask rafters = new Subtask("Монтаж стропил", "Деревянные балки", Status.DONE, epic1Id,
                LocalDateTime.of(2023, 1, 4, 9, 0), Duration.ofHours(6));
        int subtask1Id = manager.createSubtask(rafters);

        Subtask roofing = new Subtask("Укладка кровли", "Металлочерепица", Status.IN_PROGRESS, epic1Id,
                LocalDateTime.of(2023, 1, 4, 15, 0), Duration.ofHours(8));
        int subtask2Id = manager.createSubtask(roofing);

        Epic emptyEpic = new Epic("Ландшафтный дизайн", "Планировка участка");
        int epic2Id = manager.createEpic(emptyEpic);

        System.out.println("\n+++ Запросы (история должна быть без дублей) +++");
        manager.getTask(task1Id);
        manager.getEpic(epic1Id);
        manager.getSubtask(subtask1Id);
        printHistory(manager);

        manager.getTask(task2Id);
        manager.getEpic(epic2Id);
        manager.getSubtask(subtask2Id);
        printHistory(manager);

        manager.getTask(task1Id);
        printHistory(manager);

        System.out.println("\n+++ Удаление задачи 1 +++");
        manager.deleteTask(task1Id);
        printHistory(manager);

        System.out.println("\n+++ Обновление статуса задачи 2 +++");
        walls.setStatus(Status.IN_PROGRESS);
        manager.updateTask(walls);
        Optional<Task> updatedTask = manager.getTask(task2Id);
        updatedTask.ifPresent(t -> System.out.println("Статус задачи 2: " + t.getStatus()));

        System.out.println("\n+++ Попытка создать подзадачу с несуществующим эпиком +++");
        try {
            Subtask invalidSubtask = new Subtask("Невалидная подзадача", "Описание", Status.NEW, 999,
                    LocalDateTime.now(), Duration.ofHours(1));
            manager.createSubtask(invalidSubtask);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n+++ Время выполнения эпиков +++");
        Optional<Epic> roofEpicOpt = manager.getEpic(epic1Id);
        roofEpicOpt.ifPresent(epic -> {
            System.out.println("Эпик 3 (крыша):");
            System.out.println("Начало: " + formatTime(epic.getStartTime()));
            System.out.println("Длительность: " + formatDuration(epic.getDuration()));
            System.out.println("Окончание: " + formatTime(epic.getEndTime()));
        });

        System.out.println("\n+++ Удаление всех подзадач +++");
        manager.deleteAllSubtasks();
        System.out.println("Подзадачи эпика 3: " + manager.getEpicSubtasks(epic1Id).size());
        printHistory(manager);

        System.out.println("\n+++ Удаление эпика 3 +++");
        manager.deleteEpic(epic1Id);
        printHistory(manager);

        System.out.println("\n+++ Обновление истории после повторных запросов +++");
        manager.getEpic(epic2Id);
        manager.getTask(task2Id);
        manager.getEpic(epic2Id);
        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История: " + manager.getHistory().stream()
                .map(Task::getId)
                .toList());
    }

    private static String formatTime(LocalDateTime time) {
        return time != null ? time.toString() : "не задано";
    }

    private static String formatDuration(Duration duration) {
        return duration != null ? duration.toHours() + " ч." : "не задана";
    }
}
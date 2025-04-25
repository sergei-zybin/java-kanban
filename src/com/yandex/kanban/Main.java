package com.yandex.kanban;

import com.yandex.kanban.model.*;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.service.Managers;


public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        System.out.println("+++ Создание объектов +++");
        Task foundation = new Task("Закладка фундамента", "Бетонная заливка", Status.IN_PROGRESS);
        int task1Id = manager.createTask(foundation);

        Task walls = new Task("Установка стен", "Каркасные стены", Status.NEW);
        int task2Id = manager.createTask(walls);

        Epic roof = new Epic("Строительство крыши", "Полная конструкция");
        int epic1Id = manager.createEpic(roof);

        Subtask rafters = new Subtask("Монтаж стропил", "Деревянные балки", Status.DONE, epic1Id);
        int subtask1Id = manager.createSubtask(rafters);

        Subtask roofing = new Subtask("Укладка кровли", "Металлочерепица", Status.IN_PROGRESS, epic1Id);
        int subtask2Id = manager.createSubtask(roofing);

        Epic emptyEpic = new Epic("Ландшафтный дизайн", "Планировка участка");
        int epic2Id = manager.createEpic(emptyEpic);

        System.out.println("\n+++ Запросы (история должна быть без дублей) +++");
        manager.getTask(task1Id);
        manager.getEpic(epic1Id);
        manager.getSubtask(subtask1Id);
        printHistory();

        manager.getTask(task2Id);
        manager.getEpic(epic2Id);
        manager.getSubtask(subtask2Id);
        printHistory();

        manager.getTask(task1Id);
        printHistory();

        System.out.println("\n+++ Удаление задачи 1 +++");
        manager.deleteTask(task1Id);
        printHistory();

        System.out.println("\n+++ Обновление статуса задачи 2 +++");
        walls.setStatus(Status.IN_PROGRESS);
        manager.updateTask(walls);
        System.out.println("Статус задачи 2: " + manager.getTask(task2Id).getStatus());

        System.out.println("\n+++ Попытка создать подзадачу с несуществующим эпиком +++");
        try {
            Subtask invalidSubtask = new Subtask("Невалидная подзадача", "Описание", Status.NEW, 999);
            manager.createSubtask(invalidSubtask);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n+++ Удаление всех подзадач +++");
        manager.deleteAllSubtasks();
        System.out.println("Подзадачи эпика 3: " + manager.getEpicSubtasks(epic1Id).size());
        printHistory();

        System.out.println("\n+++ Удаление эпика 3 +++");
        manager.deleteEpic(epic1Id);
        printHistory();

        System.out.println("\n+++ Обновление истории после повторных запросов +++");
        manager.getEpic(epic2Id);
        manager.getTask(task2Id);
        manager.getEpic(epic2Id);
        printHistory();
    }

    private static void printHistory() {
        System.out.println("История: " + Managers.getDefault().getHistory().stream()
                .map(Task::getId)
                .toList());
    }

}
package com.yandex.kanban;

import com.yandex.kanban.model.*;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.service.Managers;
import java.util.Scanner;
import java.util.List;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {
        while (true) {
            printMenu();
            int command = scanner.nextInt();
            scanner.nextLine();

            switch (command) {
                case 1 -> createTask();
                case 2 -> createSubtask();
                case 3 -> createEpic();
                case 4 -> showTask();
                case 5 -> showSubtask();
                case 6 -> showEpic();
                case 7 -> showEpicSubtasks();
                case 8 -> showAllTasks();
                case 9 -> showAllSubtasks();
                case 10 -> showAllEpics();
                case 11 -> updateTask();
                case 12 -> updateSubtask();
                case 13 -> updateEpic();
                case 14 -> deleteTask();
                case 15 -> deleteSubtask();
                case 16 -> deleteEpic();
                case 17 -> deleteAllTasks();
                case 18 -> deleteAllSubtasks();
                case 19 -> deleteAllEpics();
                case 20 -> createTestData();
                case 21 -> showHistory();
                case 0 -> {
                    System.out.println("\nВыход из приложения");
                    return;
                }
                default -> System.out.println("Неизвестная команда");
            }
        }
    }

    private static void printMenu() {
        System.out.println("*** ТРЕКЕР ЗАДАЧ v 0.2.1 ***");
        String[][] menuColumns = {
                {"[ 1] Создать задачу",    "[ 8] Все задачи",         "[15] Удалить подзадачу"},
                {"[ 2] Создать подзадачу", "[ 9] Все подзадачи",      "[16] Удалить эпик"},
                {"[ 3] Создать эпик",      "[10] Все эпики",          "[17] Удалить все задачи"},
                {"[ 4] Показать задачу",   "[11] Обновить задачу",    "[18] Удалить все подзадачи"},
                {"[ 5] Показать подзадачу","[12] Обновить подзадачу", "[19] Удалить все эпики"},
                {"[ 6] Показать эпик",     "[13] Обновить эпик",      "[20] Тестовые данные"},
                {"[ 7] Подзадачи эпика",   "[14] Удалить задачу",     "[21] История просмотров"},
                {"",                       "",                        "[ 0] Выход"}
        };

        for (String[] row : menuColumns) {
            System.out.printf("%-23s %-23s %-23s%n", row[0], row[1], row[2]);
        }

        System.out.print("Выберите команду: ");
    }

    private static String inputName() {
        System.out.print("Введите название: ");
        return scanner.nextLine();
    }

    private static String inputDescription() {
        System.out.print("Введите описание: ");
        return scanner.nextLine();
    }

    private static int inputId(String prompt) {
        System.out.print(prompt);
        int id = scanner.nextInt();
        scanner.nextLine();
        return id;
    }

    private static void createTask() {
        try {
            String name = inputName();
            String desc = inputDescription();
            Status status = getStatus();
            Task task = new Task(name, desc, status);
            int id = manager.createTask(task);
            System.out.println("Создана задача с ID: " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void createSubtask() {
        try {
            int epicId = inputId("Введите ID эпика: ");
            if (manager.getEpic(epicId) == null) {
                System.out.println("Эпик не найден!");
                return;
            }
            String name = inputName();
            String desc = inputDescription();
            Status status = getStatus();
            Subtask subtask = new Subtask(name, desc, status, epicId);
            int id = manager.createSubtask(subtask);
            System.out.println(id == -1 ? "Ошибка!" : "Создана подзадача с ID: " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void createEpic() {
        try {
            String name = inputName();
            String desc = inputDescription();
            Epic epic = new Epic(name, desc);
            int id = manager.createEpic(epic);
            System.out.println("Создан эпик с ID: " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateTask() {
        try {
            int id = inputId("Введите ID задачи: ");
            Task task = manager.getTask(id);
            if (task == null) {
                System.out.println("Задача не найдена!");
                return;
            }
            updateTaskLogic(task, manager::updateTask);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateSubtask() {
        try {
            int id = inputId("Введите ID подзадачи: ");
            Subtask subtask = manager.getSubtask(id);
            if (subtask == null) {
                System.out.println("Подзадача не найдена!");
                return;
            }
            updateTaskLogic(subtask, updatedSubtask -> manager.updateSubtask((Subtask) updatedSubtask));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateEpic() {
        try {
            int id = inputId("Введите ID эпика: ");
            Epic epic = manager.getEpic(id);
            if (epic == null) {
                System.out.println("Эпик не найден!");
                return;
            }
            String name = inputName();
            String desc = inputDescription();
            Epic newEpic = new Epic(name, desc);
            newEpic.setId(id);
            manager.updateEpic(newEpic);
            System.out.println("Эпик обновлен");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void updateTaskLogic(Task task, java.util.function.Consumer<Task> updateFunction) {
        String name = inputName();
        String desc = inputDescription();
        Status status = getStatus();

        Task updatedTask;
        if (task instanceof Subtask subtask) {
            updatedTask = new Subtask(name, desc, status, subtask.getEpicId());
        } else {
            updatedTask = new Task(name, desc, status);
        }
        updatedTask.setId(task.getId());
        updateFunction.accept(updatedTask);
        System.out.println("Обновлено");
    }

    private static void deleteTask() {
        try {
            int id = inputId("Введите ID задачи: ");
            manager.deleteTask(id);
            System.out.println("Задача удалена");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void deleteSubtask() {
        try {
            int id = inputId("Введите ID подзадачи: ");
            manager.deleteSubtask(id);
            System.out.println("Подзадача удалена");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void deleteEpic() {
        try {
            int id = inputId("Введите ID эпика: ");
            manager.deleteEpic(id);
            System.out.println("Эпик удален");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void deleteAllTasks() {
        manager.deleteAllTasks();
        System.out.println("Все задачи удалены");
    }

    private static void deleteAllSubtasks() {
        manager.deleteAllSubtasks();
        System.out.println("Все подзадачи удалены");
    }

    private static void deleteAllEpics() {
        manager.deleteAllEpics();
        System.out.println("Все эпики удалены");
    }

    private static void showAllTasks() {
        System.out.println("\nВсе задачи:");
        manager.getAllTasks().forEach(System.out::println);
    }

    private static void showAllSubtasks() {
        System.out.println("\nВсе подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);
    }

    private static void showAllEpics() {
        System.out.println("\nВсе эпики:");
        manager.getAllEpics().forEach(System.out::println);
    }

    private static void showTask() {
        int id = inputId("Введите ID задачи: ");
        System.out.println(manager.getTask(id));
    }

    private static void showSubtask() {
        int id = inputId("Введите ID подзадачи: ");
        System.out.println(manager.getSubtask(id));
    }

    private static void showEpic() {
        int id = inputId("Введите ID эпика: ");
        System.out.println(manager.getEpic(id));
    }

    private static void showEpicSubtasks() {
        int id = inputId("Введите ID эпика: ");
        System.out.println("Подзадачи эпика:");
        manager.getEpicSubtasks(id).forEach(System.out::println);
    }

    private static Status getStatus() {
        System.out.println("Выберите статус:");
        System.out.println("1. NEW");
        System.out.println("2. IN_PROGRESS");
        System.out.println("3. DONE");
        System.out.print("> ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return switch (choice) {
            case 1 -> Status.NEW;
            case 2 -> Status.IN_PROGRESS;
            case 3 -> Status.DONE;
            default -> Status.NEW;
        };
    }

    private static void showHistory() {
        List<Task> history = manager.getHistory();
        System.out.println("\nПоследние 10 просмотренных задач:");
        if (history.isEmpty()) {
            System.out.println("История пуста.");
            return;
        }
        int counter = 1;
        for (Task task : history) {
            String type =
                    task instanceof Subtask ? "Subtask" :
                            task instanceof Epic ? "Epic" : "Task";

            System.out.printf("%2d. ID:%-4d [%s] %s%n",
                    counter++,
                    task.getId(),
                    type,
                    task.getName()
            );
        }
    }

    private static void createTestData() {
        manager.clearAll();
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.createEpic(epic);

        Status[] subStatuses = {Status.NEW, Status.IN_PROGRESS, Status.DONE};
        for (int i = 1; i <= 3; i++) {
            Subtask subtask = new Subtask(
                    "Subtask " + i,
                    "Desc " + i,
                    subStatuses[i-1],
                    epicId
            );
            manager.createSubtask(subtask);
        }

        Status[] taskStatuses = {Status.NEW, Status.IN_PROGRESS, Status.DONE};
        for (int i = 1; i <= 3; i++) {
            Task task = new Task(
                    "Task " + i,
                    "Desc " + i,
                    taskStatuses[i-1]
            );
            manager.createTask(task);
        }
        System.out.println("Тестовые задачи загружены");
    }
}



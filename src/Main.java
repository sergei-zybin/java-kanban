import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TaskManager manager = new TaskManager();

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
                case 18 -> createTestData();
                case 0 -> {
                    System.out.println("Выход");
                    return;
                }
                default -> System.out.println("Неизвестная команда");
            }
        }
    }

    private static void printMenu() {
        String menu = """
            
            Доступные команды:
            
             [1] - Создать задачу
             [2] - Создать подзадачу
             [3] - Создать эпик
            
             [4] - Показать задачу
             [5] - Показать подзадачу
             [6] - Показать эпик
             [7] - Показать подзадачи эпика
            
             [8] - Показать все задачи
             [9] - Показать все подзадачи
            [10] - Показать все эпики
            
            [11] - Обновить задачу
            [12] - Обновить подзадачу
            [13] - Обновить эпик
            
            [14] - Удалить задачу
            [15] - Удалить подзадачу
            [16] - Удалить эпик
            [17] - Удалить все задачи
            
            [18] - Загрузить тестовые задачи
            
            [0] - Выход
            
            Выберите команду:\s""";
        System.out.print(menu);
    }

    private static void createTask() {
        System.out.print("Введите название: ");
        String name = scanner.nextLine();
        System.out.print("Введите описание: ");
        String desc = scanner.nextLine();
        Status status = getStatus();
        Task task = new Task(name, desc, status);
        int id = manager.createTask(task);
        System.out.println("Создана задача с ID: " + id);
    }

    private static void createSubtask() {
        System.out.print("Введите ID эпика: ");
        int epicId = scanner.nextInt();
        scanner.nextLine();
        if (manager.getEpic(epicId) == null) {
            System.out.println("Эпик не найден!");
            return;
        }
        System.out.print("Введите название: ");
        String name = scanner.nextLine();
        System.out.print("Введите описание: ");
        String desc = scanner.nextLine();
        Status status = getStatus();
        Subtask subtask = new Subtask(name, desc, status, epicId);
        int id = manager.createSubtask(subtask);
        if (id == -1) System.out.println("Ошибка создания подзадачи!");
        else System.out.println("Создана подзадача с ID: " + id);
    }

    private static void createEpic() {
        System.out.print("Введите название: ");
        String name = scanner.nextLine();
        System.out.print("Введите описание: ");
        String desc = scanner.nextLine();
        Epic epic = new Epic(name, desc);
        int id = manager.createEpic(epic);
        System.out.println("Создан эпик с ID: " + id);
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

    private static void updateTask() {
        System.out.print("Введите ID задачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Task task = manager.getTask(id);
        if (task == null) {
            System.out.println("Задача не найдена!");
            return;
        }
        System.out.print("Новое название: ");
        String name = scanner.nextLine();
        System.out.print("Новое описание: ");
        String desc = scanner.nextLine();
        Status status = getStatus();
        Task newTask = new Task(name, desc, status);
        newTask.setId(id);
        manager.updateTask(newTask);
        System.out.println("Задача обновлена");
    }

    private static void updateSubtask() {
        System.out.print("Введите ID подзадачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Subtask subtask = manager.getSubtask(id);
        if (subtask == null) {
            System.out.println("Подзадача не найдена!");
            return;
        }
        System.out.print("Новое название: ");
        String name = scanner.nextLine();
        System.out.print("Новое описание: ");
        String desc = scanner.nextLine();
        Status status = getStatus();
        Subtask newSubtask = new Subtask(name, desc, status, subtask.getEpicId());
        newSubtask.setId(id);
        manager.updateSubtask(newSubtask);
        System.out.println("Подзадача обновлена");
    }

    private static void updateEpic() {
        System.out.print("Введите ID эпика: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        Epic epic = manager.getEpic(id);
        if (epic == null) {
            System.out.println("Эпик не найден!");
            return;
        }
        System.out.print("Новое название: ");
        String name = scanner.nextLine();
        System.out.print("Новое описание: ");
        String desc = scanner.nextLine();
        Epic newEpic = new Epic(name, desc);
        newEpic.setId(id);
        manager.updateEpic(newEpic);
        System.out.println("Эпик обновлен");
    }

    private static void deleteTask() {
        System.out.print("Введите ID задачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        manager.deleteTask(id);
        System.out.println("Задача удалена");
    }

    private static void deleteSubtask() {
        System.out.print("Введите ID подзадачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        manager.deleteSubtask(id);
        System.out.println("Подзадача удалена");
    }

    private static void deleteEpic() {
        System.out.print("Введите ID эпика: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        manager.deleteEpic(id);
        System.out.println("Эпик удален");
    }

    private static void deleteAllTasks() {
        manager.deleteAllTasks();
        System.out.println("Все задачи удалены");
    }

    private static void showTask() {
        System.out.print("Введите ID задачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println(manager.getTask(id));
    }

    private static void showSubtask() {
        System.out.print("Введите ID подзадачи: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println(manager.getSubtask(id));
    }

    private static void showEpic() {
        System.out.print("Введите ID эпика: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.println(manager.getEpic(id));
    }

    private static void showEpicSubtasks() {
        System.out.print("Введите ID эпика: ");
        int id = scanner.nextInt();
        scanner.nextLine();
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

    private static void createTestData() {

        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        int epicId = manager.createEpic(epic);

        Status[] subStatuses = {Status.NEW, Status.IN_PROGRESS, Status.DONE};
        for (int i = 1; i <= 3; i++) {
            Subtask subtask = new Subtask(
                    "Тестовая подзадача " + i,
                    "Описание тестовой подзадачи " + i,
                    subStatuses[i-1],
                    epicId
            );
            manager.createSubtask(subtask);
        }

        Status[] taskStatuses = {Status.NEW, Status.IN_PROGRESS, Status.DONE};
        for (int i = 1; i <= 3; i++) {
            Task task = new Task(
                    "Тестовая задача " + i,
                    "Описание тестовой задачи " + i,
                    taskStatuses[i-1]
            );
            manager.createTask(task);
        }
        System.out.println("Тестовые задачи загружены");
    }

}
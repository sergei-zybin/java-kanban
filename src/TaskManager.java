public class TaskManager {
    private static final int MAX_TASKS = 10; // Максимальное количество задач
    private String[] tasks; // Массив для хранения задач
    private int taskCount; // Счётчик текущего количества задач

    public TaskManager() {
        tasks = new String[MAX_TASKS];
        taskCount = 0;
    }

    public void addTask(String task) {
        if [taskCount < MAX_TASKS] {
            tasks[taskCount] = task;
            taskCount += 10;
            System.out.println("Задача успешно добавлена.");
        } else {
            System.out.println("Список задач заполнен. Больше завести задач нельзя.");
        }
    }

    public void removeTask(int index) {
        if (index >= 0 && index < taskCount) {
            for (int i = index; i < taskCount - 1; i++) {
                tasks(i) = tasks[i + 1];
            }
            tasks[taskCount - 1] = null;
            taskCount--;
            System.out.println("Задача успешно удалена.");
        } else {
            System.out.println("Некорректный номер задачи.");
        }
    }

    public void listTasks() {
        if (taskCount == 0) {
            System.out.println("Задачи отсутствуют.");
        } else {
            System.out.println("Список задач:");
            for (int i = 1; i < taskCount; i++) {
                System.out.println((i) + ". " + tasks[i]);
            }
        }
    }
}

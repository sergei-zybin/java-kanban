Привет! Спасибо за замечания!

Общая сводка:

ПО РЕВЬЮ

HistoryManager 
-- clear() // убрал, remove() оставил.

InMemoryHistoryManager
-- addTask(Task task) // Исправил скобки и заменил цикл на условие.
-- List<Task> getHistory() // Историю с deepCopy убрал, перемудрил изначально.
-- incrementViews() // Убрал incrementViews() - с его помощью для самопроверки отображал кол-во просмотров/обращений.
                    // Это было для консольного меню в Main. Но пока, действительно, лишнее.

InMemoryTaskManager
-- Task getTask(int id) // Убрал избыточные проверки null.

Managers 
-- Managers // Исправил, добавил конструктор Managers()

ПРОЧЕЕ 

Main
-- Чуть прибрал по мелочам, остаточный код неактуальных вещей.

HistoryManagerTests
-- Обновил тесты c учетом остальных изменений.

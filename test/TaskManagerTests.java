package com.yandex.kanban.service;

import com.yandex.kanban.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTests {
    private TaskManager manager;
    private Epic testEpic;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
        testEpic = new Epic("Тестовый эпик", "Описание");
        testEpic.setId(manager.createEpic(testEpic));
    }

     @Test
    void taskInHistoryShouldBeImmutable() {
        history.add(task1);
        task1.setStatus(Status.DONE);

        assertEquals(Status.NEW,
                history.getHistory().getFirst().getStatus(),
                "История должна хранить неизменяемые копии задач");
}
}



package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.model.Task;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("История задач")
class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Добавление задачи в историю")
    void testAddTaskToHistory() {
        Task task = new Task("Задача", "Описание");
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0), "Добавленная задача должна быть в истории");
    }

    @Test
    @DisplayName("Ограничение размера истории")
    void testHistorySize() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            historyManager.add(task);
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "Размер истории должен быть ограничен 10 задачами");
    }

    @Test
    @DisplayName("Изменяемость задачи в истории")
    void testTaskMutabilityInHistory() {
        Task task = new Task("Задача", "Описание");
        historyManager.add(task);
        task.setName("Обновленная задача");
        task.setDescription("Обновленное описание");
        List<Task> history = historyManager.getHistory();
        Task taskInHistory = history.get(0);
        assertEquals("Обновленная задача", taskInHistory.getName(), "Название задачи в истории должно измениться");
        assertEquals("Обновленное описание", taskInHistory.getDescription(), "Описание задачи в истории должно измениться");
    }
}
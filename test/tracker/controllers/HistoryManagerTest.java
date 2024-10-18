package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.model.Task;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HistoryManager Tests")
class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Test empty history")
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty initially");
    }

    @Test
    @DisplayName("Test add and get history")
    void testAddAndGetHistory() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofHours(1), LocalDateTime.now());
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Duration.ofHours(2), LocalDateTime.now().plusHours(2));
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(task1, history.get(0), "First task in history should be task1");
        assertEquals(task2, history.get(1), "Second task in history should be task2");
    }

    @Test
    @DisplayName("Test duplicate tasks in history")
    void testDuplicateTasks() {
        Task task = new Task("Task", "Description", Duration.ofHours(1), LocalDateTime.now());
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain only one instance of the task");
        assertEquals(task, history.get(0), "The task in history should be the added task");
    }

    @Test
    @DisplayName("Test remove from history")
    void testRemoveFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofHours(1), LocalDateTime.now());
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Duration.ofHours(2), LocalDateTime.now().plusHours(2));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Duration.ofHours(3), LocalDateTime.now().plusHours(4));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Remove from the beginning
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks after removal");
        assertEquals(task2, history.get(0), "First task should be task2 after removal");

        // Remove from the middle
        historyManager.add(task1);
        historyManager.remove(task2.getId());
        history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks after removal");
        assertEquals(task3, history.get(0), "First task should be task3 after removal");
        assertEquals(task1, history.get(1), "Second task should be task1 after removal");

        // Remove from the end
        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain 1 task after removal");
        assertEquals(task3, history.get(0), "Remaining task should be task3");
    }

    @Test
    @DisplayName("Test history order")
    void testHistoryOrder() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofHours(1), LocalDateTime.now());
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description 2", Duration.ofHours(2), LocalDateTime.now().plusHours(2));
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description 3", Duration.ofHours(3), LocalDateTime.now().plusHours(4));
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);  // Move task1 to the end

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "History should contain 3 tasks");
        assertEquals(task2, history.get(0), "First task should be task2");
        assertEquals(task3, history.get(1), "Second task should be task3");
        assertEquals(task1, history.get(2), "Third task should be task1");
    }
}
package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.model.Task;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HistoryManager Tests")
class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Add task to history")
    void testAddTaskToHistory() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain one task");
        assertEquals(task, history.get(0), "Added task should be in history");
    }

    @Test
    @DisplayName("Remove duplicates from history")
    void testRemoveDuplicatesFromHistory() {
        Task task = new Task("Task", "Description");
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain only one task");
    }

    @Test
    @DisplayName("Remove task from history")
    void testRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        task1.setId(1);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "History should contain one task");
        assertEquals(task2, history.get(0), "Only the second task should remain in history");
    }

    @Test
    @DisplayName("Order of tasks in history")
    void testHistoryOrder() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);  // Move task1 to the end
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "History should contain three tasks");
        assertEquals(task2, history.get(0), "Task 2 should be first");
        assertEquals(task3, history.get(1), "Task 3 should be second");
        assertEquals(task1, history.get(2), "Task 1 should be last");
    }
}
package tracker.model;
import tracker.controllers.TaskManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tracker.controllers.Managers;
class TaskTest {

    @Test
    void testTaskEquality() {
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(1, "Task 1", "Description 1", Status.NEW);
        assertEquals(task1, task2, "Tasks with the same id should be equal");
    }

    @Test
    void testSubtaskEquality() {
        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", Status.NEW, 1);
        Subtask subtask2 = new Subtask(1, "Subtask 1", "Description 1", Status.NEW, 1);
        assertEquals(subtask1, subtask2, "Subtasks with the same id should be equal");
    }

    @Test
    void testTaskImmutabilityAfterAddingToManager() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Task", "Description");
        String initialName = task.getName();
        String initialDescription = task.getDescription();
        Status initialStatus = task.getStatus();

        taskManager.addTask(task);

        assertEquals(initialName, task.getName(), "Task name should remain unchanged after adding to manager");
        assertEquals(initialDescription, task.getDescription(), "Task description should remain unchanged after adding to manager");
        assertEquals(initialStatus, task.getStatus(), "Task status should remain unchanged after adding to manager");
    }
}
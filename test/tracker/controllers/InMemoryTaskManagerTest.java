package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryTaskManager Tests")
class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    @DisplayName("Add task")
    void testAddTask() {
        Task task = new Task("Task", "Description");
        Task addedTask = taskManager.addTask(task);
        assertNotNull(addedTask.getId(), "Task id should be generated");
        assertEquals(task, taskManager.getTaskByID(addedTask.getId()), "Added task should be retrievable by id");
    }

    @Test
    @DisplayName("Add epic")
    void testAddEpic() {
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);
        assertNotNull(addedEpic.getId(), "Epic id should be generated");
        assertEquals(epic, taskManager.getEpicByID(addedEpic.getId()), "Added epic should be retrievable by id");
    }

    @Test
    @DisplayName("Add subtask")
    void testAddSubtask() {
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", addedEpic.getId());
        Subtask addedSubtask = taskManager.addSubtask(subtask);
        assertNotNull(addedSubtask.getId(), "Subtask id should be generated");
        assertEquals(subtask, taskManager.getSubtaskByID(addedSubtask.getId()), "Added subtask should be retrievable by id");
    }

    @Test
    @DisplayName("Delete task")
    void testDeleteTask() {
        Task task = taskManager.addTask(new Task("Task", "Description"));
        taskManager.deleteTaskByID(task.getId());
        assertNull(taskManager.getTaskByID(task.getId()), "Task should be deleted");
        assertTrue(taskManager.getHistory().isEmpty(), "Task should be removed from history");
    }

    @Test
    @DisplayName("Delete epic")
    void testDeleteEpic() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Subtask", "Description", epic.getId()));
        taskManager.deleteEpicByID(epic.getId());
        assertNull(taskManager.getEpicByID(epic.getId()), "Epic should be deleted");
        assertNull(taskManager.getSubtaskByID(subtask.getId()), "Subtask should be deleted");
        assertTrue(taskManager.getHistory().isEmpty(), "Epic and subtask should be removed from history");
    }

    @Test
    @DisplayName("Data integrity on deletion")
    void testDataIntegrityOnDeletion() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Subtask", "Description", epic.getId()));
        taskManager.deleteSubtaskByID(subtask.getId());
        assertFalse(taskManager.getEpicByID(epic.getId()).getSubtaskList().contains(subtask),
                "Subtask should be removed from epic's subtask list");
    }

    @Test
    @DisplayName("Update task")
    void testUpdateTask() {
        Task task = taskManager.addTask(new Task("Task", "Description"));
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Updated Task", updatedTask.getName(), "Task name should be updated");
        assertEquals("Updated Description", updatedTask.getDescription(), "Task description should be updated");
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Task status should be updated");
    }

    @Test
    @DisplayName("Task history")
    void testTaskHistory() {
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1"));
        Task task2 = taskManager.addTask(new Task("Task 2", "Description 2"));
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.getTaskByID(task1.getId());
        assertEquals(2, taskManager.getHistory().size(), "History should contain 2 tasks");
        assertEquals(task1, taskManager.getHistory().get(1), "Last viewed task should be last in history");
    }
}
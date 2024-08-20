package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void testAddTask() {
        Task task = new Task("Task", "Description");
        Task addedTask = taskManager.addTask(task);
        assertNotNull(addedTask.getId(), "Task id should be generated");
        assertEquals(task, taskManager.getTaskByID(addedTask.getId()), "Added task should be retrievable by id");
    }

    @Test
    void testAddEpic() {
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);
        assertNotNull(addedEpic.getId(), "Epic id should be generated");
        assertEquals(epic, taskManager.getEpicByID(addedEpic.getId()), "Added epic should be retrievable by id");
    }

    @Test
    void testAddSubtask() {
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", addedEpic.getId());
        Subtask addedSubtask = taskManager.addSubtask(subtask);
        assertNotNull(addedSubtask.getId(), "Subtask id should be generated");
        assertEquals(subtask, taskManager.getSubtaskByID(addedSubtask.getId()), "Added subtask should be retrievable by id");
    }

    @Test
    void testTaskWithAssignedId() {
        Task task = new Task(1, "Task", "Description", Status.NEW);
        Task addedTask = taskManager.addTask(task);
        assertEquals(1, addedTask.getId(), "Task should retain the assigned id");
        assertEquals(task, taskManager.getTaskByID(1), "Task with assigned id should be retrievable");
    }

    @Test
    void testManagersUtilityClass() {
        TaskManager defaultManager = Managers.getDefault();
        assertNotNull(defaultManager, "Default task manager should be initialized");
        assertTrue(defaultManager instanceof InMemoryTaskManager, "Default task manager should be an instance of InMemoryTaskManager");

        HistoryManager defaultHistory = Managers.getDefaultHistory();
        assertNotNull(defaultHistory, "Default history manager should be initialized");
        assertTrue(defaultHistory instanceof InMemoryHistoryManager, "Default history manager should be an instance of InMemoryHistoryManager");
    }
}
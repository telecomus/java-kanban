package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void testAddTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Test Task", "Test Description", Duration.ofHours(1), now);
        Task addedTask = taskManager.addTask(task);

        assertNotNull(addedTask.getId());
        assertEquals(task.getName(), addedTask.getName());
        assertEquals(task.getDescription(), addedTask.getDescription());
        assertEquals(task.getStatus(), addedTask.getStatus());
        assertEquals(task.getDuration(), addedTask.getDuration());
        assertEquals(task.getStartTime(), addedTask.getStartTime());
    }

    @Test
    void testAddEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Epic addedEpic = taskManager.addEpic(epic);

        assertNotNull(addedEpic.getId());
        assertEquals(epic.getName(), addedEpic.getName());
        assertEquals(epic.getDescription(), addedEpic.getDescription());
        assertEquals(Status.NEW, addedEpic.getStatus());
    }

    @Test
    void testAddSubtask() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId(), Duration.ofHours(1), now);
        Subtask addedSubtask = taskManager.addSubtask(subtask);

        assertNotNull(addedSubtask.getId());
        assertEquals(subtask.getName(), addedSubtask.getName());
        assertEquals(subtask.getDescription(), addedSubtask.getDescription());
        assertEquals(subtask.getStatus(), addedSubtask.getStatus());
        assertEquals(subtask.getDuration(), addedSubtask.getDuration());
        assertEquals(subtask.getStartTime(), addedSubtask.getStartTime());
        assertEquals(epic.getId(), addedSubtask.getEpicID());
    }

    @Test
    void testUpdateTask() {
        Task task = taskManager.addTask(new Task("Test Task", "Test Description", Duration.ofHours(1), LocalDateTime.now()));
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(Status.IN_PROGRESS);

        Task updatedTask = taskManager.updateTask(task);

        assertEquals("Updated Task", updatedTask.getName());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void testUpdateEpic() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        epic.setName("Updated Epic");
        epic.setDescription("Updated Description");

        Epic updatedEpic = taskManager.updateEpic(epic);

        assertEquals("Updated Epic", updatedEpic.getName());
        assertEquals("Updated Description", updatedEpic.getDescription());
    }

    @Test
    void testUpdateSubtask() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test Subtask", "Test Description", epic.getId(), Duration.ofHours(1), LocalDateTime.now()));
        subtask.setName("Updated Subtask");
        subtask.setDescription("Updated Description");
        subtask.setStatus(Status.IN_PROGRESS);

        Subtask updatedSubtask = taskManager.updateSubtask(subtask);

        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals("Updated Description", updatedSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, updatedSubtask.getStatus());
    }

    @Test
    void testDeleteTask() {
        Task task = taskManager.addTask(new Task("Test Task", "Test Description", Duration.ofHours(1), LocalDateTime.now()));
        taskManager.deleteTaskByID(task.getId());

        assertNull(taskManager.getTaskByID(task.getId()));
    }

    @Test
    void testDeleteEpic() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test Subtask", "Test Description", epic.getId(), Duration.ofHours(1), LocalDateTime.now()));

        taskManager.deleteEpicByID(epic.getId());

        assertNull(taskManager.getEpicByID(epic.getId()));
        assertNull(taskManager.getSubtaskByID(subtask.getId()));
    }

    @Test
    void testDeleteSubtask() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test Subtask", "Test Description", epic.getId(), Duration.ofHours(1), LocalDateTime.now()));

        taskManager.deleteSubtaskByID(subtask.getId());

        assertNull(taskManager.getSubtaskByID(subtask.getId()));
        assertFalse(taskManager.getEpicByID(epic.getId()).getSubtaskList().contains(subtask));
    }

    @Test
    void testGetHistory() {
        LocalDateTime now = LocalDateTime.now();
        Task task = taskManager.addTask(new Task("Test Task", "Test Description", Duration.ofHours(1), now));
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask = taskManager.addSubtask(new Subtask("Test Subtask", "Test Description", epic.getId(), Duration.ofHours(1), now.plusHours(2)));

        taskManager.getTaskByID(task.getId());
        taskManager.getEpicByID(epic.getId());
        taskManager.getSubtaskByID(subtask.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
    }

    @Test
    void testGetPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1", Duration.ofHours(1), now.plusHours(1)));
        Task task2 = taskManager.addTask(new Task("Task 2", "Description 2", Duration.ofHours(1), now));
        Task task3 = taskManager.addTask(new Task("Task 3", "Description 3", Duration.ofHours(1), null));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size());
        assertEquals(task2, prioritizedTasks.get(0));
        assertEquals(task1, prioritizedTasks.get(1));
    }

    @Test
    void testEpicStatus() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(), Duration.ofHours(1), LocalDateTime.now()));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask 2", "Description 2", epic.getId(), Duration.ofHours(1), LocalDateTime.now().plusHours(2)));

        // Test when all subtasks are NEW
        assertEquals(Status.NEW, taskManager.getEpicByID(epic.getId()).getStatus());

        // Test when all subtasks are DONE
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.DONE, taskManager.getEpicByID(epic.getId()).getStatus());

        // Test when subtasks have mixed status
        subtask1.setStatus(Status.NEW);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(epic.getId()).getStatus());

        // Test when a subtask is IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(epic.getId()).getStatus());
    }

    @Test
    void testEpicSubtaskRelationship() {
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Test Subtask 1", "Test Description", epic.getId(), Duration.ofHours(1), LocalDateTime.now()));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Test Subtask 2", "Test Description", epic.getId(), Duration.ofHours(1), LocalDateTime.now().plusHours(2)));

        // Проверка связи подзадач с эпиком
        assertEquals(epic.getId(), subtask1.getEpicID(), "Subtask1 should have correct epic ID");
        assertEquals(epic.getId(), subtask2.getEpicID(), "Subtask2 should have correct epic ID");

        // Проверка наличия подзадач в списке эпика
        Epic retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertTrue(retrievedEpic.getSubtaskList().contains(subtask1), "Epic should contain subtask1");
        assertTrue(retrievedEpic.getSubtaskList().contains(subtask2), "Epic should contain subtask2");

        // Проверка обновления эпика при изменении подзадачи
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Status.IN_PROGRESS, retrievedEpic.getStatus(), "Epic status should be updated when subtask status changes");

        // Проверка удаления подзадачи из эпика
        taskManager.deleteSubtaskByID(subtask1.getId());
        retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertFalse(retrievedEpic.getSubtaskList().contains(subtask1), "Epic should not contain deleted subtask");
        assertEquals(1, retrievedEpic.getSubtaskList().size(), "Epic should have only one subtask left");
    }

    @Test
    void testTaskIntersection() {
        LocalDateTime now = LocalDateTime.now();

        // Проверка пересечения для обычных задач
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1", Duration.ofHours(2), now));
        Task task2 = new Task("Task 2", "Description 2", Duration.ofHours(2), now.plusHours(1));

        try {
            Task addedTask2 = taskManager.addTask(task2);
            fail("Expected IllegalArgumentException, but task was added successfully: " + addedTask2);
        } catch (IllegalArgumentException e) {
            assertEquals("The task overlaps in time with another task", e.getMessage());
        }

        // Проверка пересечения для подзадач
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(2), now.plusHours(3)));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofHours(2), now.plusHours(4));

        try {
            Subtask addedSubtask2 = taskManager.addSubtask(subtask2);
            fail("Expected IllegalArgumentException, but subtask was added successfully: " + addedSubtask2);
        } catch (IllegalArgumentException e) {
            assertEquals("The subtask overlaps in time with another task", e.getMessage());
        }

        // Проверка пересечения между задачей и подзадачей
        Task task3 = new Task("Task 3", "Description 3", Duration.ofHours(2), now.plusHours(3));
        try {
            Task addedTask3 = taskManager.addTask(task3);
            fail("Expected IllegalArgumentException, but task was added successfully: " + addedTask3);
        } catch (IllegalArgumentException e) {
            assertEquals("The task overlaps in time with another task", e.getMessage());
        }

        // Вывод информации о всех задачах для диагностики
        System.out.println("All tasks after test:");
        for (Task task : taskManager.getPrioritizedTasks()) {
            System.out.println(task);
        }
    }

    @Test
    void testEpicTimeCalculationAfterSubtaskDeletion() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = taskManager.addEpic(new Epic("Test Epic", "Test Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(2), now));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofHours(3), now.plusHours(3)));

        Epic retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Duration.ofHours(5), retrievedEpic.getDuration(), "Epic duration should be sum of subtasks durations");
        assertEquals(now, retrievedEpic.getStartTime(), "Epic start time should be the earliest subtask start time");
        assertEquals(now.plusHours(6), retrievedEpic.getEndTime(), "Epic end time should be the latest subtask end time");

        // Удаление первой подзадачи
        taskManager.deleteSubtaskByID(subtask1.getId());
        retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Duration.ofHours(3), retrievedEpic.getDuration(), "Epic duration should be updated after subtask deletion");
        assertEquals(now.plusHours(3), retrievedEpic.getStartTime(), "Epic start time should be updated after subtask deletion");
        assertEquals(now.plusHours(6), retrievedEpic.getEndTime(), "Epic end time should remain the same after first subtask deletion");

        // Удаление второй (последней) подзадачи
        taskManager.deleteSubtaskByID(subtask2.getId());
        retrievedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(Duration.ZERO, retrievedEpic.getDuration(), "Epic duration should be zero after all subtasks are deleted");
        assertNull(retrievedEpic.getStartTime(), "Epic start time should be null after all subtasks are deleted");
        assertNull(retrievedEpic.getEndTime(), "Epic end time should be null after all subtasks are deleted");
    }
}
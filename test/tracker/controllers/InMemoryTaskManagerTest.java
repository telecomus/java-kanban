package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task", "Description", Duration.ofHours(1), now);
        Task addedTask = taskManager.addTask(task);
        assertNotNull(addedTask.getId(), "Task id should be generated");
        assertEquals(task, taskManager.getTaskByID(addedTask.getId()), "Added task should be retrievable by id");
        assertEquals(Duration.ofHours(1), addedTask.getDuration(), "Task duration should be set correctly");
        assertEquals(now, addedTask.getStartTime(), "Task start time should be set correctly");
        assertEquals(now.plusHours(1), addedTask.getEndTime(), "Task end time should be calculated correctly");
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
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = new Subtask("Subtask", "Description", addedEpic.getId(), Duration.ofMinutes(30), now);
        Subtask addedSubtask = taskManager.addSubtask(subtask);
        assertNotNull(addedSubtask.getId(), "Subtask id should be generated");
        assertEquals(subtask, taskManager.getSubtaskByID(addedSubtask.getId()), "Added subtask should be retrievable by id");
        assertEquals(Duration.ofMinutes(30), addedSubtask.getDuration(), "Subtask duration should be set correctly");
        assertEquals(now, addedSubtask.getStartTime(), "Subtask start time should be set correctly");
        assertEquals(now.plusMinutes(30), addedSubtask.getEndTime(), "Subtask end time should be calculated correctly");
    }

    @Test
    @DisplayName("Delete task")
    void testDeleteTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task = taskManager.addTask(new Task("Task", "Description", Duration.ofHours(1), now));
        taskManager.deleteTaskByID(task.getId());
        assertNull(taskManager.getTaskByID(task.getId()), "Task should be deleted");
        assertTrue(taskManager.getHistory().isEmpty(), "Task should be removed from history");
    }

    @Test
    @DisplayName("Delete epic")
    void testDeleteEpic() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = taskManager.addSubtask(new Subtask("Subtask", "Description", epic.getId(), Duration.ofMinutes(30), now));
        taskManager.deleteEpicByID(epic.getId());
        assertNull(taskManager.getEpicByID(epic.getId()), "Epic should be deleted");
        assertNull(taskManager.getSubtaskByID(subtask.getId()), "Subtask should be deleted");
        assertTrue(taskManager.getHistory().isEmpty(), "Epic and subtask should be removed from history");
    }

    @Test
    @DisplayName("Data integrity on deletion")
    void testDataIntegrityOnDeletion() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask = taskManager.addSubtask(new Subtask("Subtask", "Description", epic.getId(), Duration.ofMinutes(30), now));
        taskManager.deleteSubtaskByID(subtask.getId());
        assertFalse(taskManager.getEpicByID(epic.getId()).getSubtaskList().contains(subtask),
                "Subtask should be removed from epic's subtask list");
    }

    @Test
    @DisplayName("Update task")
    void testUpdateTask() {
        LocalDateTime now = LocalDateTime.now();
        Task task = taskManager.addTask(new Task("Task", "Description", Duration.ofHours(1), now));
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(Status.IN_PROGRESS);
        task.setDuration(Duration.ofHours(2));
        task.setStartTime(now.plusHours(1));
        taskManager.updateTask(task);
        Task updatedTask = taskManager.getTaskByID(task.getId());
        assertEquals("Updated Task", updatedTask.getName(), "Task name should be updated");
        assertEquals("Updated Description", updatedTask.getDescription(), "Task description should be updated");
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Task status should be updated");
        assertEquals(Duration.ofHours(2), updatedTask.getDuration(), "Task duration should be updated");
        assertEquals(now.plusHours(1), updatedTask.getStartTime(), "Task start time should be updated");
        assertEquals(now.plusHours(3), updatedTask.getEndTime(), "Task end time should be updated");
    }

    @Test
    @DisplayName("Task history")
    void testTaskHistory() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1", Duration.ofHours(1), now));
        Task task2 = taskManager.addTask(new Task("Task 2", "Description 2", Duration.ofHours(2), now.plusHours(2)));
        taskManager.getTaskByID(task1.getId());
        taskManager.getTaskByID(task2.getId());
        taskManager.getTaskByID(task1.getId());
        assertEquals(2, taskManager.getHistory().size(), "History should contain 2 tasks");
        assertEquals(task1, taskManager.getHistory().get(1), "Last viewed task should be last in history");
    }

    @Test
    @DisplayName("Prioritized tasks")
    void testPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1", Duration.ofHours(1), now.plusHours(1)));
        Task task2 = taskManager.addTask(new Task("Task 2", "Description 2", Duration.ofHours(1), now.plusHours(2)));
        Task task3 = taskManager.addTask(new Task("Task 3", "Description 3", Duration.ofHours(1), now));
        Task taskWithoutStartTime = taskManager.addTask(new Task("Task 4", "Description 4", Duration.ofHours(1), null));

        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(1), now.plusMinutes(30)));

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasks.size(), "Prioritized tasks should contain 4 tasks");
        assertEquals(task3, prioritizedTasks.get(0), "Task 3 should be first");
        assertEquals(subtask1, prioritizedTasks.get(1), "Subtask 1 should be second");
        assertEquals(task1, prioritizedTasks.get(2), "Task 1 should be third");
        assertEquals(task2, prioritizedTasks.get(3), "Task 2 should be fourth");
        assertFalse(prioritizedTasks.contains(taskWithoutStartTime), "Task without start time should not be in prioritized list");
    }

    @Test
    @DisplayName("Epic time calculation")
    void testEpicTimeCalculation() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(1), now));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofHours(2), now.plusHours(2)));

        Epic updatedEpic = taskManager.getEpicByID(epic.getId());
        assertEquals(now, updatedEpic.getStartTime(), "Epic start time should be the earliest subtask start time");
        assertEquals(Duration.ofHours(3), updatedEpic.getDuration(), "Epic duration should be sum of subtasks durations");
        assertEquals(now.plusHours(4), updatedEpic.getEndTime(), "Epic end time should be the latest subtask end time");
    }

    @Test
    @DisplayName("Epic status update")
    void testEpicStatusUpdate() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(1), now));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofHours(1), now.plusHours(2)));

        assertEquals(Status.NEW, taskManager.getEpicByID(epic.getId()).getStatus(), "Epic status should be NEW");

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(epic.getId()).getStatus(), "Epic status should be IN_PROGRESS");

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.DONE, taskManager.getEpicByID(epic.getId()).getStatus(), "Epic status should be DONE");
    }
}
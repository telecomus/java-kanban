package tracker.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryTaskManager Tests")
class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
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
    @DisplayName("Prioritized tasks")
    void testPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        // Задачи без пересечений по времени
        Task task1 = taskManager.addTask(new Task("Task 1", "Description 1", Duration.ofHours(1), now.plusHours(1)));
        Task task2 = taskManager.addTask(new Task("Task 2", "Description 2", Duration.ofHours(1), now.plusHours(2)));
        Task task3 = taskManager.addTask(new Task("Task 3", "Description 3", Duration.ofHours(1), now));

        // Эпик и подзадача, не пересекающаяся с другими задачами
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = taskManager.addSubtask(
                new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(1), now.plusHours(3))
        );

        Task taskWithoutStartTime = taskManager.addTask(
                new Task("Task 4", "Description 4", Duration.ofHours(1), null)
        );

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(4, prioritizedTasks.size(), "Prioritized tasks should contain 4 tasks");
        assertEquals(task3, prioritizedTasks.get(0), "Task 3 should be first");
        assertEquals(task1, prioritizedTasks.get(1), "Task 1 should be second");
        assertEquals(task2, prioritizedTasks.get(2), "Task 2 should be third");
        assertEquals(subtask1, prioritizedTasks.get(3), "Subtask 1 should be fourth");
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
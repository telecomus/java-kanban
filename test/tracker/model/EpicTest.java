package tracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.controllers.TaskManager;
import tracker.controllers.Managers;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

@DisplayName("Tests for the Epic class")
class EpicTest {

    @Test
    @DisplayName("Equality of Epics")
    void testEpicEquality() {
        Epic epic1 = new Epic(1, "Epic 1", "Description 1");
        Epic epic2 = new Epic(1, "Epic 1", "Description 1");
        assertEquals(epic1, epic2, "Epics with the same id should be equal");
    }

    @Test
    @DisplayName("Subtasks of an Epic")
    void testEpicSubtasks() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime1 = LocalDateTime.now();
        LocalDateTime startTime2 = startTime1.plusHours(2);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId(), Duration.ofHours(1), startTime1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId(), Duration.ofHours(1), startTime2);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, taskManager.getEpicSubtasks(addedEpic).size(), "Epic should contain two subtasks");
        assertTrue(taskManager.getEpicSubtasks(addedEpic).contains(subtask1), "Epic should contain subtask1");
        assertTrue(taskManager.getEpicSubtasks(addedEpic).contains(subtask2), "Epic should contain subtask2");
    }

    @Test
    @DisplayName("Epic status - all subtasks NEW")
    void testEpicStatusAllNew() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId(), Duration.ofHours(1), startTime);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId(), Duration.ofHours(1), startTime.plusHours(2));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.NEW, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be NEW when all subtasks are NEW");
    }

    @Test
    @DisplayName("Epic status - all subtasks DONE")
    void testEpicStatusAllDone() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId(), Duration.ofHours(1), startTime);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId(), Duration.ofHours(1), startTime.plusHours(2));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(Status.DONE, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be DONE when all subtasks are DONE");
    }

    @Test
    @DisplayName("Epic status - subtasks NEW and DONE")
    void testEpicStatusNewAndDone() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId(), Duration.ofHours(1), startTime);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId(), Duration.ofHours(1), startTime.plusHours(2));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be IN_PROGRESS when subtasks have NEW and DONE statuses");
    }

    @Test
    @DisplayName("Epic status - subtask IN_PROGRESS")
    void testEpicStatusInProgress() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();
        Subtask subtask = new Subtask("Subtask", "Description", addedEpic.getId(), Duration.ofHours(1), startTime);

        taskManager.addSubtask(subtask);

        subtask.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be IN_PROGRESS when any subtask is IN_PROGRESS");
    }

    @Test
    @DisplayName("Calculating the duration and time of an epic")
    void testEpicDurationAndTime() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        LocalDateTime startTime1 = LocalDateTime.now();
        LocalDateTime startTime2 = startTime1.plusHours(2);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId(), Duration.ofHours(1), startTime1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId(), Duration.ofHours(1), startTime2);

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicByID(addedEpic.getId());
        assertEquals(Duration.ofHours(2), updatedEpic.getDuration(), "Epic duration should be sum of subtasks durations");
        assertEquals(startTime1, updatedEpic.getStartTime(), "Epic start time should be the earliest subtask start time");
        assertEquals(startTime2.plusHours(1), updatedEpic.getEndTime(), "Epic end time should be the latest subtask end time");
    }

    @Test
    @DisplayName("Epic without subtasks")
    void testEmptyEpic() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Empty Epic", "No subtasks");
        Epic addedEpic = taskManager.addEpic(epic);

        assertEquals(Status.NEW, addedEpic.getStatus(), "Empty Epic should have NEW status");
        assertNull(addedEpic.getStartTime(), "Empty Epic should have null start time");
        assertNull(addedEpic.getEndTime(), "Empty Epic should have null end time");
        assertEquals(Duration.ZERO, addedEpic.getDuration(), "Empty Epic should have zero duration");
    }
}
package tracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.controllers.TaskManager;
import tracker.controllers.Managers;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для класса Epic")
class EpicTest {

    @Test
    @DisplayName("Равенство эпиков")
    void testEpicEquality() {
        Epic epic1 = new Epic(1, "Epic 1", "Description 1");
        Epic epic2 = new Epic(1, "Epic 1", "Description 1");
        assertEquals(epic1, epic2, "Epics with the same id should be equal");
    }

    @Test
    @DisplayName("Подзадачи эпика")
    void testEpicSubtasks() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(2, taskManager.getEpicSubtasks(addedEpic).size(), "Epic should contain two subtasks");
        assertTrue(taskManager.getEpicSubtasks(addedEpic).contains(subtask1), "Epic should contain subtask1");
        assertTrue(taskManager.getEpicSubtasks(addedEpic).contains(subtask2), "Epic should contain subtask2");
    }

    @Test
    @DisplayName("Статус эпика")
    void testEpicStatus() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Epic", "Description");
        Epic addedEpic = taskManager.addEpic(epic);

        assertEquals(Status.NEW, addedEpic.getStatus(), "Epic status should be NEW by default");

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", addedEpic.getId());
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.addSubtask(subtask1);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be IN_PROGRESS if any subtask is IN_PROGRESS");

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", addedEpic.getId());
        subtask2.setStatus(Status.DONE);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be IN_PROGRESS if subtasks have mixed statuses");

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        assertEquals(Status.DONE, taskManager.getEpicByID(addedEpic.getId()).getStatus(), "Epic status should be DONE if all subtasks are DONE");
    }
}
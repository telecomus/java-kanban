package tracker.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private File file;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        file = tempDir.resolve("tasks.csv").toFile();
        return new FileBackedTaskManager(file);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(0, loadedManager.getTasks().size(), "The loaded manager must be empty");
        assertEquals(0, loadedManager.getEpics().size(), "The loaded manager must be empty");
        assertEquals(0, loadedManager.getSubtasks().size(), "The loaded manager must be empty");
    }

    @Test
    void testSaveAndLoadTasks() {
        LocalDateTime now = LocalDateTime.now().withNano(0);  // Убираем наносекунды для сравнения
        Task task = new Task("Task 1", "Description 1", Duration.ofHours(1), now);
        Epic epic = new Epic("Epic 1", "Description 2");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId(), Duration.ofMinutes(30), now.plusHours(1));

        taskManager.addTask(task);
        taskManager.addSubtask(subtask);

        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getTasks().size(), "The loaded manager must contain 1 task");
        assertEquals(1, loadedManager.getEpics().size(), "The uploaded manager must contain 1 epic");
        assertEquals(1, loadedManager.getSubtasks().size(), "The loaded manager must contain 1 subtask");

        Task loadedTask = loadedManager.getTasks().get(0);
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getDuration(), loadedTask.getDuration());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());

        Subtask loadedSubtask = loadedManager.getSubtasks().get(0);
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration());
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
    }

    @Test
    void testSaveAndLoadHistory() {
        LocalDateTime now = LocalDateTime.now().withNano(0);  // Убираем наносекунды для сравнения
        Task task = new Task("Task 1", "Description 1", Duration.ofHours(1), now);
        Epic epic = new Epic("Epic 1", "Description 2");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId(), Duration.ofMinutes(30), now.plusHours(1));

        taskManager.addTask(task);
        taskManager.addSubtask(subtask);

        taskManager.getTaskByID(task.getId());
        taskManager.getEpicByID(epic.getId());
        taskManager.getSubtaskByID(subtask.getId());

        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> history = loadedManager.getHistory();
        assertEquals(3, history.size(), "The story must contain 3 tasks");
        assertTrue(history.stream().anyMatch(t -> t.getId() == task.getId()), "The story must contain a task");
        assertTrue(history.stream().anyMatch(t -> t.getId() == epic.getId()), "The story must contain an epic");
        assertTrue(history.stream().anyMatch(t -> t.getId() == subtask.getId()), "The story must contain a subtask");
    }

    @Test
    void testSaveExceptionHandling() {
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(new File("/invalid/path/tasks.csv"));
        Task task = new Task("Test Task", "Test Description", Duration.ofHours(1), LocalDateTime.now());

        assertThrows(ManagerSaveException.class, () -> invalidManager.addTask(task));
    }

    @Test
    void testLoadExceptionHandling() {
        File nonExistentFile = new File("/non/existent/file.csv");

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(nonExistentFile));
    }

    @Test
    void testEpicStatusUpdateAfterLoad() {
        Epic epic = taskManager.addEpic(new Epic("Epic", "Description"));
        LocalDateTime now = LocalDateTime.now();
        Subtask subtask1 = taskManager.addSubtask(new Subtask("Subtask 1", "Description", epic.getId(), Duration.ofHours(1), now));
        Subtask subtask2 = taskManager.addSubtask(new Subtask("Subtask 2", "Description", epic.getId(), Duration.ofHours(1), now.plusHours(2)));

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Epic loadedEpic = loadedManager.getEpicByID(epic.getId());
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "Epic status should be IN_PROGRESS after loading");
    }
}
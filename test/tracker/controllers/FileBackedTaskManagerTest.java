package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;
    private File file;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(file);
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(0, loadedManager.getTasks().size(), "Загруженный менеджер должен быть пустым");
        assertEquals(0, loadedManager.getEpics().size(), "Загруженный менеджер должен быть пустым");
        assertEquals(0, loadedManager.getSubtasks().size(), "Загруженный менеджер должен быть пустым");
    }

    @Test
    void testSaveAndLoadTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task 1", "Description 1", Duration.ofHours(1), now);
        Epic epic = new Epic("Epic 1", "Description 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId(), Duration.ofMinutes(30), now.plusHours(1));

        manager.addTask(task);
        manager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loadedManager.getTasks().size(), "Загруженный менеджер должен содержать 1 задачу");
        assertEquals(1, loadedManager.getEpics().size(), "Загруженный менеджер должен содержать 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Загруженный менеджер должен содержать 1 подзадачу");

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

        Epic loadedEpic = loadedManager.getEpics().get(0);
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertEquals(subtask.getDuration(), loadedEpic.getDuration());
        assertEquals(subtask.getStartTime(), loadedEpic.getStartTime());
        assertEquals(subtask.getEndTime(), loadedEpic.getEndTime());
    }

    @Test
    void testSaveAndLoadHistory() {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task("Task 1", "Description 1", Duration.ofHours(1), now);
        Epic epic = new Epic("Epic 1", "Description 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId(), Duration.ofMinutes(30), now.plusHours(1));

        manager.addTask(task);
        manager.addSubtask(subtask);

        manager.getTaskByID(task.getId());
        manager.getEpicByID(epic.getId());
        manager.getSubtaskByID(subtask.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> history = loadedManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать 3 задачи");
        assertTrue(history.contains(task), "История должна содержать задачу");
        assertTrue(history.contains(epic), "История должна содержать эпик");
        assertTrue(history.contains(subtask), "История должна содержать подзадачу");
    }
}
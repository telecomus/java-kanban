package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        Task task = new Task("Task 1", "Description 1");
        Epic epic = new Epic("Epic 1", "Description 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId());

        manager.addTask(task);
        manager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loadedManager.getTasks().size(), "Загруженный менеджер должен содержать 1 задачу");
        assertEquals(1, loadedManager.getEpics().size(), "Загруженный менеджер должен содержать 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Загруженный менеджер должен содержать 1 подзадачу");
    }

    @Test
    void testSaveAndLoadHistory() {
        Task task = new Task("Task 1", "Description 1");
        Epic epic = new Epic("Epic 1", "Description 2");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description 3", epic.getId());

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
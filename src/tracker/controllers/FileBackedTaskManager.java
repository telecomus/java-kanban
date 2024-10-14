package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    public void save() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("id,type,name,status,description,epic\n");

            // Сохранение задач
            for (Task task : getTasks()) {
                csv.append(task.toString()).append("\n");
            }

            // Сохранение эпиков и подзадач
            for (Epic epic : getEpics()) {
                csv.append(epic.toString()).append("\n");
                for (Subtask subtask : epic.getSubtaskList()) {
                    csv.append(subtask.toString()).append("\n");
                }
            }

            // Сохранение истории просмотров
            for (Task task : getHistory()) {
                csv.append(task.getId()).append(",").append(task.getClass().getSimpleName()).append("\n");
            }

            Files.writeString(file.toPath(), csv.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String csv = Files.readString(file.toPath());
            String[] lines = csv.split("\n");

            // Чтение эпиков
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("id,type,name,status,description,epic")) {
                    continue;
                }
                String[] fields = line.split(",");
                if (fields[1].equals("EPIC")) {
                    manager.addEpic(Epic.fromString(line));
                }
            }

            // Чтение задач и подзадач
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("id,type,name,status,description,epic")) {
                    continue;
                }
                String[] fields = line.split(",");
                switch (fields[1]) {
                    case "TASK":
                        manager.addTask(Task.fromString(line));
                        break;
                    case "SUBTASK":
                        manager.addSubtask(Subtask.fromString(line));
                        break;
                }
            }

            // Чтение истории просмотров
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("id,type,name,status,description,epic")) {
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length == 2) {
                    int taskId = Integer.parseInt(fields[0]);
                    switch (fields[1]) {
                        case "Task":
                            manager.getTaskByID(taskId);
                            break;
                        case "Epic":
                            manager.getEpicByID(taskId);
                            break;
                        case "Subtask":
                            manager.getSubtaskByID(taskId);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла", e);
        }
        return manager;
    }

    @Override
    public Task addTask(Task task) {
        Task added = super.addTask(task);
        save();
        return added;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic added = super.addEpic(epic);
        save();
        return added;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        Epic epic = getEpicByID(subtask.getEpicID());
        if (epic == null) {
            throw new IllegalArgumentException("Epic not found for subtask: " + subtask);
        }
        Subtask added = super.addSubtask(subtask);
        save();
        return added;
    }

    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updated = super.updateSubtask(subtask);
        save();
        return updated;
    }

    @Override
    public void deleteTaskByID(int id) {
        super.deleteTaskByID(id);
        save();
    }

    @Override
    public void deleteEpicByID(int id) {
        super.deleteEpicByID(id);
        save();
    }

    @Override
    public void deleteSubtaskByID(int id) {
        super.deleteSubtaskByID(id);
        save();
    }

    @Override
    public Task getTaskByID(int id) {
        Task task = super.getTaskByID(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicByID(int id) {
        Epic epic = super.getEpicByID(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        Subtask subtask = super.getSubtaskByID(id);
        save();
        return subtask;
    }
}
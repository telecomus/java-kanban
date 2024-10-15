package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    public void save() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append(Constants.CSV_HEADER).append("\n");

            // Сохранение задач
            for (Task task : getTasks()) {
                csv.append(task.toString()).append("\n");
            }

            // Сохранение эпиков
            for (Epic epic : getEpics()) {
                csv.append(epic.toString()).append("\n");
            }

            // Сохранение подзадач
            for (Subtask subtask : getSubtasks()) {
                csv.append(subtask.toString()).append("\n");
            }

            // Сохранение истории просмотров
            csv.append("\n");
            for (Task task : getHistory()) {
                csv.append(task.getId()).append(",");
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

            boolean isHistory = false;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    isHistory = true;
                    continue;
                }
                if (!isHistory) {
                    Task task = fromString(line);
                    if (task != null) {
                        if (task instanceof Epic) {
                            manager.epics.put(task.getId(), (Epic) task);
                        } else if (task instanceof Subtask) {
                            Subtask subtask = (Subtask) task;
                            manager.subtasks.put(subtask.getId(), subtask);
                            Epic epic = manager.epics.get(subtask.getEpicID());
                            if (epic != null) {
                                epic.addSubtask(subtask);
                            }
                        } else {
                            manager.tasks.put(task.getId(), task);
                        }
                    }
                } else {
                    String[] ids = line.split(",");
                    for (String id : ids) {
                        if (!id.isEmpty()) {
                            int taskId = Integer.parseInt(id);
                            Task task = manager.findTaskById(taskId);
                            if (task != null) {
                                manager.historyManager.add(task);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла", e);
        }
        return manager;
    }

    private Task findTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
        }
        if (task == null) {
            task = subtasks.get(id);
        }
        return task;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 3) {
            return null;
        }
        switch (parts[1]) {
            case "TASK":
                return Task.fromString(value);
            case "EPIC":
                return Epic.fromString(value);
            case "SUBTASK":
                return Subtask.fromString(value);
            default:
                return null;
        }
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
package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.Status;
import tracker.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    @Override
    public void save() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append(Constants.CSV_HEADER).append("\n");

            for (Task task : getTasks()) {
                csv.append(toString(task)).append("\n");
            }

            for (Epic epic : getEpics()) {
                csv.append(toString(epic)).append("\n");
            }

            for (Subtask subtask : getSubtasks()) {
                csv.append(toString(subtask)).append("\n");
            }

            csv.append("\n");
            for (Task task : historyManager.getHistory()) {
                csv.append(task.getId()).append(",");
            }

            Files.writeString(file.toPath(), csv.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл", e);
        }
    }

    private String toString(Task task) {
        String baseString = String.format("%d,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task instanceof Epic ? "EPIC" : task instanceof Subtask ? "SUBTASK" : "TASK",
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                task.getDuration() != null ? task.getDuration().toMinutes() : "",
                task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "");

        if (task instanceof Subtask) {
            baseString += "," + ((Subtask) task).getEpicID();
        }

        return baseString;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String csv = Files.readString(file.toPath());
            String[] lines = csv.split("\n");

            AtomicBoolean isHistory = new AtomicBoolean(false);
            Arrays.stream(lines)
                    .skip(1)
                    .map(String::trim)
                    .forEach(line -> {
                        if (line.isEmpty()) {
                            isHistory.set(true);
                        } else if (!isHistory.get()) {
                            Task task = fromString(line);
                            if (task != null) {
                                if (task instanceof Epic) {
                                    manager.epics.put(task.getId(), (Epic) task);
                                } else if (task instanceof Subtask) {
                                    Subtask subtask = (Subtask) task;
                                    manager.subtasks.put(subtask.getId(), subtask);
                                    manager.epics.computeIfPresent(subtask.getEpicID(), (id, epic) -> {
                                        epic.addSubtask(subtask);
                                        return epic;
                                    });
                                } else {
                                    manager.tasks.put(task.getId(), task);
                                }
                                manager.addToPrioritizedTasks(task);
                            }
                        }
                    });

            if (isHistory.get()) {
                String historyLine = Arrays.stream(lines)
                        .filter(line -> !line.isEmpty())
                        .reduce((first, second) -> second)
                        .orElse("");
                Arrays.stream(historyLine.split(","))
                        .filter(id -> !id.isEmpty())
                        .map(Integer::parseInt)
                        .map(manager::findTaskById)
                        .filter(Objects::nonNull)
                        .forEach(manager.historyManager::add);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки данных из файла", e);
        }
        return manager;
    }
    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 7) {
            return null;
        }
        int id = Integer.parseInt(parts[0]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        Duration duration = parts[5].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[5]));
        LocalDateTime startTime = null;
        try {
            startTime = parts[6].isEmpty() ? null : LocalDateTime.parse(parts[6], DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Ошибка парсинга даты и времени: " + parts[6]);
        }

        switch (parts[1]) {
            case "TASK":
                return new Task(id, name, description, status, duration, startTime);
            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                if (duration != null) {
                    epic.setDuration(duration);
                }
                if (startTime != null) {
                    epic.setStartTime(startTime);
                }
                return epic;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[7]);
                return new Subtask(id, name, description, status, epicId, duration, startTime);
            default:
                return null;
        }
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

    @Override
    public Task addTask(Task task) {
        if (task.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .anyMatch(t -> isIntersect(task, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
            }
        }
        Task added = super.addTask(task);
        save();
        return added;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .anyMatch(t -> isIntersect(epic, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Эпик пересекается по времени с другой задачей");
            }
        }
        Epic added = super.addEpic(epic);
        save();
        return added;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .anyMatch(t -> isIntersect(subtask, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }
        }
        Subtask added = super.addSubtask(subtask);
        save();
        return added;
    }

    @Override
    public Task updateTask(Task task) {
        if (task.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .filter(t -> t.getId() != task.getId())
                    .anyMatch(t -> isIntersect(task, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Задача пересекается по времени с другой задачей");
            }
        }
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .filter(t -> t.getId() != epic.getId())
                    .anyMatch(t -> isIntersect(epic, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Эпик пересекается по времени с другой задачей");
            }
        }
        Epic updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null) {
            List<Task> prioritizedTasks = getPrioritizedTasks();
            boolean hasIntersection = prioritizedTasks.stream()
                    .filter(t -> t.getId() != subtask.getId())
                    .anyMatch(t -> isIntersect(subtask, t));
            if (hasIntersection) {
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }
        }
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return super.getPrioritizedTasks();
    }
}
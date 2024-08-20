package tracker.controllers;

import tracker.model.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        Task taskCopy = createTaskCopy(task);
        history.add(taskCopy);
        if (history.size() > 10) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public Task createTaskCopy(Task task) {
        return new Task(task.getName(), task.getDescription());
    }
}
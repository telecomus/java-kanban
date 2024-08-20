package tracker.controllers;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
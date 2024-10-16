package tracker.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import tracker.controllers.TaskManager;
import tracker.controllers.Managers;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

@DisplayName("Тесты для класса Task")
class TaskTest {

    @Test
    @DisplayName("Равенство задач")
    void testTaskEquality() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);
        Task task1 = new Task(1, "Task 1", "Description 1", Status.NEW, duration, startTime);
        Task task2 = new Task(1, "Task 1", "Description 1", Status.NEW, duration, startTime);
        assertEquals(task1, task2, "Tasks with the same id and fields should be equal");
    }

    @Test
    @DisplayName("Неизменяемость задачи после добавления в менеджер")
    void testTaskImmutabilityAfterAddingToManager() {
        TaskManager taskManager = Managers.getDefault();
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);
        Task task = new Task("Task", "Description", duration, startTime);
        String initialName = task.getName();
        String initialDescription = task.getDescription();
        Status initialStatus = task.getStatus();
        Duration initialDuration = task.getDuration();
        LocalDateTime initialStartTime = task.getStartTime();

        taskManager.addTask(task);

        assertEquals(initialName, task.getName(), "Task name should remain unchanged after adding to manager");
        assertEquals(initialDescription, task.getDescription(), "Task description should remain unchanged after adding to manager");
        assertEquals(initialStatus, task.getStatus(), "Task status should remain unchanged after adding to manager");
        assertEquals(initialDuration, task.getDuration(), "Task duration should remain unchanged after adding to manager");
        assertEquals(initialStartTime, task.getStartTime(), "Task start time should remain unchanged after adding to manager");
    }

    @Test
    @DisplayName("Проверка расчета времени окончания задачи")
    void testTaskEndTime() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);
        Task task = new Task("Task", "Description", duration, startTime);

        assertEquals(startTime.plus(duration), task.getEndTime(), "End time should be start time plus duration");
    }
}
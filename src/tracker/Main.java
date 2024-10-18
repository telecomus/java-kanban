package tracker;

import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // Создание обычной задачи
        LocalDateTime packLuggageStart = LocalDateTime.now().plusDays(1);
        Task packLuggage = new Task("Собрать чемодан", "Не забыть документы", Duration.ofHours(2), packLuggageStart);
        taskManager.addTask(packLuggage);

        // Создание эпика
        Epic europeTour = new Epic("Путешествие по Европе", "Посетить главные достопримечательности");
        taskManager.addEpic(europeTour);

        // Создание подзадач для эпика
        LocalDateTime bookHotelsStart = LocalDateTime.now().plusDays(2);
        Subtask bookHotels = new Subtask("Забронировать отели", "Выбрать центральные локации", europeTour.getId(), Duration.ofHours(3), bookHotelsStart);

        LocalDateTime buyTicketsStart = LocalDateTime.now().plusDays(3);
        Subtask buyTickets = new Subtask("Купить билеты на самолет", "Поискать выгодные предложения", europeTour.getId(), Duration.ofHours(2), buyTicketsStart);

        taskManager.addSubtask(bookHotels);
        taskManager.addSubtask(buyTickets);

        // Вывод информации о задачах
        System.out.println("Информация о задачах:");
        printTaskInfo(taskManager.getTaskByID(packLuggage.getId()));
        printTaskInfo(taskManager.getEpicByID(europeTour.getId()));
        printTaskInfo(taskManager.getSubtaskByID(bookHotels.getId()));
        printTaskInfo(taskManager.getSubtaskByID(buyTickets.getId()));

        // Обновление статуса подзадачи
        buyTickets.setStatus(Status.DONE);
        taskManager.updateSubtask(buyTickets);

        // Вывод обновленной информации об эпике
        System.out.println("\nОбновленная информация об эпике после выполнения подзадачи:");
        printTaskInfo(taskManager.getEpicByID(europeTour.getId()));

        // Вывод истории просмотров
        System.out.println("\nИстория просмотров задач:");
        List<Task> history = taskManager.getHistory();
        for (Task task : history) {
            System.out.println(task.getName());
        }
    }

    private static void printTaskInfo(Task task) {
        System.out.println("Название: " + task.getName());
        System.out.println("Описание: " + task.getDescription());
        System.out.println("Статус: " + task.getStatus());
        System.out.println("Продолжительность: " + task.getDuration().toHours() + " часов");
        System.out.println("Время начала: " + task.getStartTime());
        System.out.println("Время окончания: " + task.getEndTime());
        if (task instanceof Epic epic) {
            System.out.println("Количество подзадач: " + epic.getSubtaskList().size());
        }
        System.out.println();
    }

}
package tracker;

import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Status;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // Создаем и добавляем обычную задачу
        Task packLuggage = new Task("Собрать чемодан", "Не забыть документы");
        taskManager.addTask(packLuggage);

        // Создаем и добавляем эпик
        Epic europeTour = new Epic("Путешествие по Европе", "Посетить главные достопримечательности");
        taskManager.addEpic(europeTour);

        // Создаем и добавляем подзадачи к эпику
        Subtask bookHotels = new Subtask("Забронировать отели", "Выбрать центральные локации", europeTour.getId());
        Subtask buyTickets = new Subtask("Купить билеты на самолет", "Поискать выгодные предложения", europeTour.getId());
        taskManager.addSubtask(bookHotels);
        taskManager.addSubtask(buyTickets);

        // Просматриваем задачи, чтобы они попали в историю
        System.out.println("Просмотр задач:");
        System.out.println(taskManager.getTaskByID(packLuggage.getId()));
        System.out.println(taskManager.getEpicByID(europeTour.getId()));
        System.out.println(taskManager.getSubtaskByID(bookHotels.getId()));
        System.out.println(taskManager.getSubtaskByID(buyTickets.getId()));

        // Выводим историю просмотров
        System.out.println("\nИстория просмотров задач:");
        List<Task> history = taskManager.getHistory();
        for (Task task : history) {
            System.out.println(task);
        }
    }
}
public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task packLuggage = new Task("Собрать чемодан", "Не забыть документы");
        Task packLuggageCreated = taskManager.addTask(packLuggage);
        System.out.println(packLuggageCreated);

        Task packLuggageToUpdate = new Task(packLuggage.getId(), "Упаковать чемодан", "Положить теплые вещи", Status.IN_PROGRESS);
        Task packLuggageUpdated = taskManager.updateTask(packLuggageToUpdate);
        System.out.println(packLuggageUpdated);

        Epic europeTour = new Epic("Путешествие по Европе", "Посетить главные достопримечательности");
        taskManager.addEpic(europeTour);
        System.out.println(europeTour);

        Subtask europeTourSubtask1 = new Subtask("Забронировать отели", "Выбрать центральные локации", europeTour.getId());
        Subtask europeTourSubtask2 = new Subtask("Купить билеты на самолет", "Поискать выгодные предложения", europeTour.getId());

        taskManager.addSubtask(europeTourSubtask1);
        taskManager.addSubtask(europeTourSubtask2);
        System.out.println(europeTour);

        europeTourSubtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(europeTourSubtask2);
        System.out.println(europeTour);
    }
}
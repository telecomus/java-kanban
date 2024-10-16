package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtaskList = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, null, null);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, Status.NEW, null, null);
    }

    public void addSubtask(Subtask subtask) {
        subtaskList.add(subtask);
        updateTimeAndDuration();
    }

    public void removeSubtask(Subtask subtask) {
        subtaskList.remove(subtask);
        updateTimeAndDuration();
    }

    public void clearSubtasks() {
        subtaskList.clear();
        updateTimeAndDuration();
    }

    public ArrayList<Subtask> getSubtaskList() {
        return subtaskList;
    }

    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
        updateTimeAndDuration();
    }

    private void updateTimeAndDuration() {
        if (subtaskList.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            endTime = null;
            return;
        }

        LocalDateTime earliestStart = subtaskList.get(0).getStartTime();
        LocalDateTime latestEnd = subtaskList.get(0).getEndTime();
        Duration totalDuration = Duration.ZERO;

        for (Subtask subtask : subtaskList) {
            if (subtask.getStartTime() != null && (earliestStart == null || subtask.getStartTime().isBefore(earliestStart))) {
                earliestStart = subtask.getStartTime();
            }
            if (subtask.getEndTime() != null && (latestEnd == null || subtask.getEndTime().isAfter(latestEnd))) {
                latestEnd = subtask.getEndTime();
            }
            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        setStartTime(earliestStart);
        setDuration(totalDuration);
        endTime = latestEnd;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return getId() + "," + TaskType.EPIC + "," + getName() + "," + getStatus() + "," + getDescription() + ","
                + (getDuration() != null ? getDuration().toMinutes() : "") + ","
                + (getStartTime() != null ? getStartTime().toString() : "") + ","
                + (endTime != null ? endTime.toString() : "");
    }

    public static Epic fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        Epic epic = new Epic(name, description);
        epic.setId(id);
        epic.setStatus(status);
        if (!fields[5].isEmpty()) {
            epic.setDuration(Duration.ofMinutes(Long.parseLong(fields[5])));
        }
        if (!fields[6].isEmpty()) {
            epic.setStartTime(LocalDateTime.parse(fields[6]));
        }
        if (fields.length > 7 && !fields[7].isEmpty()) {
            epic.endTime = LocalDateTime.parse(fields[7]);
        }
        return epic;
    }
}
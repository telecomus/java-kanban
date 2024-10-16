package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int epicID;

    public Subtask(String name, String description, int epicID, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.epicID = epicID;
    }

    public Subtask(int id, String name, String description, Status status, int epicID, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    @Override
    public String toString() {
        return getId() + "," + TaskType.SUBTASK + "," + getName() + "," + getStatus() + "," + getDescription() + ","
                + epicID + "," + (getDuration() != null ? getDuration().toMinutes() : "") + ","
                + (getStartTime() != null ? getStartTime().toString() : "");
    }

    public static Subtask fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicID = Integer.parseInt(fields[5]);
        Duration duration = fields[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(fields[6]));
        LocalDateTime startTime = fields[7].isEmpty() ? null : LocalDateTime.parse(fields[7]);
        Subtask subtask = new Subtask(name, description, epicID, duration, startTime);
        subtask.setId(id);
        subtask.setStatus(status);
        return subtask;
    }
}
package tracker.model;

public class Subtask extends Task {

    private final int epicID;

    public Subtask(String name, String description, int epicID) {
        super(name, description);
        this.epicID = epicID;
    }

    public Subtask(int id, String name, String description, Status status, int epicID) {
        super(id, name, description, status);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    @Override
    public String toString() {
        return getId() + "," + TaskType.SUBTASK + "," + getName() + "," + getStatus() + "," + getDescription() + "," + epicID;
    }

    public static Subtask fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicID = Integer.parseInt(fields[5]);
        Subtask subtask = new Subtask(name, description, epicID);
        subtask.setId(id);
        subtask.setStatus(status);
        return subtask;
    }
}
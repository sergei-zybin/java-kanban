import java.util.*;

class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public String toString() {
        return "Epic:\n" +
                "  ID = " + id + ",\n" +
                "  Name = '" + name + "',\n" +
                "  Description = '" + description + "',\n" +
                "  Status = " + status + ",\n" +
                "  SubtaskIds = " + subtaskIds;
    }
}
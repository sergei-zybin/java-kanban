package com.yandex.kanban.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask copy() {
        Subtask copy = new Subtask(
                this.getName(),
                this.getDescription(),
                this.getStatus(),
                this.epicId
        );
        copy.setId(this.getId());
        copy.setViews(this.getViews());
        return copy;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format(
                "Subtask [id=%d, name='%s', description='%s', status=%s, views=%d, epicId=%d]",
                getId(), getName(), getDescription(), getStatus(), getViews(), epicId
        );
    }
}


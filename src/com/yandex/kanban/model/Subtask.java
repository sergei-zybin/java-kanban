package com.yandex.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(
                this.getName(),
                this.getDescription(),
                this.getStatus(),
                this.epicId,
                this.getStartTime(),
                this.getDuration()
        );
        copy.setId(this.getId());
        return copy;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format(
                "Subtask [id=%d, name='%s', description='%s', status=%s, epicId=%d, startTime=%s, duration=%s, endTime=%s]",
                getId(), getName(), getDescription(), getStatus(), epicId, getStartTime(), getDuration(), getEndTime()
        );
    }
}

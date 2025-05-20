package com.yandex.kanban.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return Collections.unmodifiableList(subtaskIds);
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.setStartTime(this.getStartTime());
        copy.setDuration(this.getDuration());
        copy.setEndTime(this.endTime);
        copy.subtaskIds.addAll(this.subtaskIds);
        return copy;
    }

    @Override
    public String toString() {
        return String.format(
                "Epic [id=%d, name='%s', description='%s', status=%s, subtaskIds=%s, startTime=%s, duration=%s, endTime=%s]",
                getId(), getName(), getDescription(), getStatus(), subtaskIds, getStartTime(), getDuration(), endTime
        );
    }
}
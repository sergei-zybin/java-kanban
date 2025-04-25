package com.yandex.kanban.model;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

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
    public Epic copy() {
        Epic copy = new Epic(this.getName(), this.getDescription());
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        copy.subtaskIds.addAll(this.subtaskIds);
        return copy;
    }

    @Override
    public String toString() {
        return String.format(
            "Epic [id=%d, name='%s', description='%s', status=%s, subtaskIds=%s]",
            getId(), getName(), getDescription(), getStatus(), subtaskIds
        );
    }
}


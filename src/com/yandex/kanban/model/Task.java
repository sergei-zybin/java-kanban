package com.yandex.kanban.model;

import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private Status status;
    private int views;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    public Task copy() {
        Task copy = new Task(this.name, this.description, this.status);
        copy.setId(this.id);
        copy.views = this.views;
        return copy;
    }

    public void setViews(int views) {
        this.views = views;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getViews() {
        return views;
    }

    public void incrementViews() {
        views++;
    }

    @Override
    public String toString() {
        return String.format(
                "Task [id=%d, name='%s', description='%s', status=%s, views=%d]",
                id, name, description, status, views
        );
    }
}
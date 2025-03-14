package com.yandex.kanban.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask:\n" +
                "  ID: " + id + "\n" +
                "  Epic ID: " + epicId + "\n" +
                "  Name: '" + name + "'\n" +
                "  Description: '" + description + "'\n" +
                "  Status: " + status;
        }
    }



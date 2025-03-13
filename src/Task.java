class Task {
    protected int id;
    protected String name;
    protected String description;
    protected Status status;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Task:\n" +
                "  Id = " + id + ",\n" +
                "  Name = '" + name + "',\n" +
                "  Description = '" + description + "',\n" +
                "  Status = " + status;
    }
}
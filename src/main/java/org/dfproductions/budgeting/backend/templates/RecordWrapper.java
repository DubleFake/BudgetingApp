package org.dfproductions.budgeting.backend.templates;

public class RecordWrapper {
    private Record record;
    private String email;

    // Getters and setters
    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "RecordWrapper{" +
                "record=" + record +
                ", email='" + email + '\'' +
                '}';
    }
}


package org.dfproductions.budgeting.backend.templates;

public class DataSingelton {
    private static final DataSingelton instance = new DataSingelton();

    private Record record;

    private DataSingelton(){}

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public static DataSingelton getInstance() {
        return instance;
    }
}

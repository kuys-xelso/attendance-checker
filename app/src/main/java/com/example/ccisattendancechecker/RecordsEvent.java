package com.example.ccisattendancechecker;

public class RecordsEvent {
    private String eventName;
    private String dateCreated;
    private String id;

    public RecordsEvent() {}

    public RecordsEvent(String eventName, String dateCreated, String id) {
        this.eventName = eventName;
        this.dateCreated = dateCreated;
        this.id = id;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}

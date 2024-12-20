package com.example.ccisattendancechecker;

import com.google.firebase.Timestamp;

public class OngoingEvent {


    private String eventId;
    private String eventName;
    private Timestamp cutOffTime;
    private String createdBy;


    public OngoingEvent() {
    }

    public OngoingEvent(String eventName, Timestamp cutOffTime, String createdBy, String eventId) {
        this.eventName = eventName;
        this.cutOffTime = cutOffTime;
        this.createdBy = createdBy;
        this.eventId = eventId;

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Timestamp getCutOffTime() {
        return cutOffTime;
    }

    public void setCutOffTime(Timestamp cutOffTime) {
        this.cutOffTime = cutOffTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
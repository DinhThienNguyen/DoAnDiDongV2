package com.example.asus.doandidongv2;

/**
 * Created by GO.Thunder on 19-Nov-17.
 */

public class Event {
    private int eventId;
    private String eventName;
    private String eventDiscript;

    public Event(){}

    public Event(int eventId, String eventName, String eventDiscript){
        this.eventId= eventId;
        this.eventName= eventName;
        this.eventDiscript= eventDiscript;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDiscript() {
        return eventDiscript;
    }

    public void setEventDiscript(String eventDiscript) {
        this.eventDiscript = eventDiscript;
    }

    @Override
    public String toString()  {
        return this.eventName;
    }
}


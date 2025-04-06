package com.example.demo.javaSrc.eventsANDtask;

import java.util.Date;
import java.io.*;

public class Event {
    enum EventType {
        EXAM, TEST, SCHOOL_EVENT, PARENTS_MEETING, PERSONAL
    }

    String title, content, locationOrLink, link;
    Task task;
    int duration;
    Date start;
    File file;
    EventType eventType;

    public Event(String title, Date start, String content, String locationOrLink, Task task, int duration, String link, File file, EventType eventType) {
        this.title = title;
        this.start = start;
        this.content = content;
        this.locationOrLink = locationOrLink;
        this.task = task;
        this.duration = duration;
        this.link = link;
        this.file = file;
        this.eventType = eventType;
    }

    public String getTitle() { return title; }
    public Date getStart() { return start; }
    public String getContent() { return content; }
    public String getLocationOrLink() { return locationOrLink; }
    public Task getTask() { return task; }
    public int getDuration() { return duration; }
    public String getLink() { return link; }
    public File getFile() { return file; }
    public EventType getEventType() { return eventType; }
}

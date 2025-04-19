package com.example.demo.javaSrc.eventsANDtask;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "events")
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id") private Long id;

    @Column(nullable = false) @JsonProperty("title") private String title;
    @Column @JsonProperty("content") private String content;
    @Column(name = "location_or_link") @JsonProperty("location_or_link") private String locationOrLink;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "task_id") @JsonProperty("task_id")
    private Task task;

    @Column(nullable = false) @JsonProperty("duration") private int duration;

    @Column(name = "start_event", nullable = false) @JsonProperty("start_event")
    private LocalDateTime startEvent;

    @Enumerated(EnumType.STRING) @Column(name = "event_type", nullable = false)
    @JsonProperty("event_type") private EventType eventType;

    @Column(name = "created_by", nullable = false) @JsonProperty("created_by") private Long createdBy;

    @Transient private java.io.File file;

    public Event() {}

    public Event(String title, LocalDateTime startEvent, String content, String locationOrLink, Task task, int duration, EventType eventType, Long createdBy) {
        this.title = title; this.startEvent = startEvent; this.content = content;
        this.locationOrLink = locationOrLink; this.task = task; this.duration = duration;
        this.eventType = eventType; this.createdBy = createdBy;
    }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public String getLocationOrLink() { return locationOrLink; } public void setLocationOrLink(String locationOrLink) { this.locationOrLink = locationOrLink; }
    public Task getTask() { return task; } public void setTask(Task task) { this.task = task; }
    public int getDuration() { return duration; } public void setDuration(int duration) { this.duration = duration; }
    public LocalDateTime getStartEvent() { return startEvent; } public void setStartEvent(LocalDateTime startEvent) { this.startEvent = startEvent; }
    public EventType getEventType() { return eventType; } public void setEventType(EventType eventType) { this.eventType = eventType; }
    public Long getCreatedBy() { return createdBy; } public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public java.io.File getFile() { return file; } public void setFile(java.io.File file) { this.file = file; }

    public enum EventType { EXAM, TEST, SCHOOL_EVENT, PARENTS_MEETING, PERSONAL }
}

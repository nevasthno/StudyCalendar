package com.example.demo.javaSrc.eventsANDtask;

import java.sql.Timestamp;

public class Invitation {
    private int id;
    private int eventId;
    private int userId;
    private Timestamp createdAt;
    private Status status;
    public enum Status  { PENDING, ACCEPTED, DECLINED }
    public Invitation(int id, int eventId, int userId, Timestamp createdAt, Status status) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    public int getId() { return id; }
    public int getEventId() { return eventId; }
    public int getUserId() { return userId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
}

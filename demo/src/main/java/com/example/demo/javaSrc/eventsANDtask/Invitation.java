package com.example.demo.javaSrc.eventsANDtask;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "invitations")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int eventId;
    private int userId;
    private Timestamp createdAt;
    private Status status;
    public enum Status  { PENDING, ACCEPTED, DECLINED }

    public Invitation() {
    }

    public Invitation(Long id, int eventId, int userId, Timestamp createdAt, Status status) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    public Long getId() { return id; }
    public int getEventId() { return eventId; }
    public int getUserId() { return userId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
}

package com.example.demo.javaSrc.eventsANDtask;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
<<<<<<< HEAD
import java.sql.*;
import java.util.Date;
import java.util.*;
=======
import jakarta.persistence.Table;
>>>>>>> 9fb88d4 (Refactor event and task management system)

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Date deadline;
    

    public Task(String title, String content, Date deadline) {
        this.title = title;
        this.content = content;
        this.deadline = deadline;
    }

    public Task() {

    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        String titleCopy = title;
        return titleCopy;
    }

    public String getContent() {
        String contentCopy = content;
        return contentCopy;
    }

    public Date getDeadline() {
        Date deadlineCopy = (Date) deadline.clone();
        return deadlineCopy;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    protected void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", deadline=" + deadline +
                '}';
    }
}



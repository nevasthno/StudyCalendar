package com.example.demo.javaSrc.eventsANDtask;

import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Date deadline;

    // нове поле:
    private boolean completed;

    public Task() {}

    public Task(String title, String content, Date deadline) {
        this.title = title;
        this.content = content;
        this.deadline = deadline;
        this.completed = false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Date getDeadline() { return (Date) deadline.clone(); }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return "Task{" +
               "title='" + title + '\'' +
               ", content='" + content + '\'' +
               ", deadline=" + deadline +
               ", completed=" + completed +
               '}';
    }
}

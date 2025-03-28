package eventsANDtask;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class Task {
    private String title;
    private String content;
    private Date deadline;
    private boolean isCompleted;

    public Task(String title, String content, Date deadline) {
        this.title = title;
        this.content = content;
        this.deadline = deadline;
        this.isCompleted = false;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Date getDeadline() {
        return deadline;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void markAsCompleted() {
        this.isCompleted = true;
    }
    
    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", deadline=" + deadline +
                ", completed=" + isCompleted +
                '}';
    }
}

class TaskManager {
    private static final String URL = "jdbc:mysql://localhost:3306/EventsDB";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678"; 
    
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, content, deadline, isCompleted) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getContent());
            stmt.setTimestamp(3, new Timestamp(task.getDeadline().getTime()));
            stmt.setBoolean(4, task.isCompleted());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY deadline";

        try (Connection conn =DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = new Task(
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("deadline")
                );
                if (rs.getBoolean("isCompleted")) {
                    task.markAsCompleted();
                }
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void markTaskAsCompleted(int taskId) {
        String sql = "UPDATE tasks SET isCompleted = TRUE WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Task> searchTasks(String keyword) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE LOWER(title) LIKE ? OR LOWER(content) LIKE ? ORDER BY deadline";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Додаю % для пошуку будь-якого входження ключового слова
            stmt.setString(1, "%" + keyword.toLowerCase() + "%");
            stmt.setString(2, "%" + keyword.toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("deadline")
                    );
                    if (rs.getBoolean("isCompleted")) {
                        task.markAsCompleted();
                    }
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    
    public List<Task> getPendingTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE isCompleted = FALSE ORDER BY deadline";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = new Task(
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("deadline")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}
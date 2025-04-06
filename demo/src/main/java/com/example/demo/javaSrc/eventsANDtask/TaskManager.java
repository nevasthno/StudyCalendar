package com.example.demo.javaSrc.eventsANDtask;

import java.sql.*;
import java.util.*;

public class TaskManager {
    private static final String URL = "jdbc:mysql://localhost:3306/PeopleAndEvents";
    private static final String USER = "root";
    private static final String PASSWORD = com.example.demo.javaSrc.Config.getPassword();
    
    public void addTask(Task task) {
        String sql = "INSERT INTO tasks (title, content, deadline) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getContent());
            stmt.setTimestamp(3, new Timestamp(task.getDeadline().getTime()));

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
           
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public void markTaskAsCompleted(int taskId, int userId) {
        String sql = "INSERT INTO user_task_status (user_id, task_id, is_completed, completed_at) " +
                     "VALUES (?, ?, TRUE, NOW()) " +
                     "ON DUPLICATE KEY UPDATE is_completed = TRUE, completed_at = NOW()";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isTaskCompletedByUser(int taskId, int userId) {
        String sql = "SELECT is_completed FROM user_task_status WHERE task_id = ? AND user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_completed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }
    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, uts.is_completed " +
                     "FROM tasks t " +
                     "LEFT JOIN user_task_status uts ON t.id = uts.task_id AND uts.user_id = ? " +
                     "ORDER BY t.deadline";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getTimestamp("deadline")
                    );
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    
    public List<Task> getTasksByDateForUser(int userId, boolean future) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, uts.is_completed " +
                     "FROM tasks t " +
                     "LEFT JOIN user_task_status uts ON t.id = uts.task_id AND uts.user_id = ? " +
                     "WHERE t.deadline " + (future ? ">=" : "<") + " NOW() " +
                     "ORDER BY t.deadline";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getTimestamp("deadline")
                    );
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> searchTasksForUser(int userId, String keyword) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, uts.is_completed " +
                     "FROM tasks t " +
                     "LEFT JOIN user_task_status uts ON t.id = uts.task_id AND uts.user_id = ? " +
                     "WHERE LOWER(t.title) LIKE ? OR LOWER(t.content) LIKE ? " +
                     "ORDER BY t.deadline";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + keyword.toLowerCase() + "%");
            stmt.setString(3, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getTimestamp("deadline")
                    );
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

}

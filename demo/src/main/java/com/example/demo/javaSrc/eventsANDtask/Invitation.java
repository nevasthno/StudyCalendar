package com.example.demo.javaSrc.eventsANDtask;

import java.sql.Timestamp;
import java.sql.*;
import java.util.*;
public class Invitation {
    private int id;
    private int eventId;
    private int userId;
    private String status;
    private Timestamp createdAt;

    public Invitation(int id, int eventId, int userId, String status, Timestamp createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getEventId() { return eventId; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
}
class InvitationManager {
    private static final String URL = "jdbc:mysql://localhost:3306/EventsDB";
    private static final String USER = "root";
    private static final String PASSWORD = com.example.demo.javaSrc.Config.getPassword();

    public static void createInvitation(int eventId, int userId) {
        String sql = "INSERT INTO invitations (event_id, user_id, status) VALUES (?, ?, 'pending')";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void acceptInvitation(int invitationId) {
        updateInvitationStatus(invitationId, "accepted");
    }

    public static void declineInvitation(int invitationId) {
        updateInvitationStatus(invitationId, "declined");
    }

    private static void updateInvitationStatus(int invitationId, String status) {
        String sql = "UPDATE invitations SET status = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, invitationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Invitation> getInvitationsForUser(int userId) {
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM invitations WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invitation invitation = new Invitation(
                        rs.getInt("id"),
                        rs.getInt("event_id"),
                        rs.getInt("user_id"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                    invitations.add(invitation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invitations;
    }
}
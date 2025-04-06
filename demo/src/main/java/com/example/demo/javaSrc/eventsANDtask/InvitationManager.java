package com.example.demo.javaSrc.eventsANDtask;

import java.sql.*;
import java.util.*;

public class InvitationManager {
    private static final String URL = "jdbc:mysql://localhost:3306/PeopleAndEvents";
    private static final String USER = "root";
    private static final String PASSWORD = com.example.demo.javaSrc.Config.getPassword();

    public static int createInvitation(int eventId, int userId) {
        String sqlInvitation = "INSERT INTO invitations (event_id, user_id) VALUES (?, ?)";
        String sqlStatus = "INSERT INTO user_invitations_status (invitation_id, user_id, status) VALUES (?, ?, 'pending')";
        int invitationId = -1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlInvitation, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, eventId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invitationId = rs.getInt(1);
                    }
                }
            }

            if (invitationId != -1) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlStatus)) {
                    stmt.setInt(1, invitationId);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
            }

            return invitationId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
 
    public static void updateInvitationStatus(int invitationId, Invitation.Status status) {
        String sql = "UPDATE user_invitations_status SET status = ? WHERE invitation_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, invitationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Invitation> getInvitationsForUser(int userId) {
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT i.id, i.event_id, i.user_id, uis.status, i.created_at " +
                    "FROM invitations i " +
                    "LEFT JOIN user_invitations_status uis ON i.id = uis.invitation_id " +
                    "WHERE i.user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invitation invitation = new Invitation(
                        rs.getInt("id"),
                        rs.getInt("event_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("created_at"),
                        Invitation.Status.valueOf(rs.getString("status").toUpperCase())
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

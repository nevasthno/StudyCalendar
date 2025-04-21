package com.example.demo.javaSrc.eventsANDtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;

    @Autowired
    public InvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public Invitation saveInvitation(Invitation invitation) {
        return invitationRepository.save(invitation);
    }

    public List<Invitation> getInvitationsForUser(Long userId) {
        return invitationRepository.findByUserId(userId);
    }

    public void updateStatus(Long invitationId, Long userId, Invitation.Status newStatus) {
        Invitation invitation = invitationRepository.findByInvitationIdAndUserId(invitationId, userId);
        if (invitation == null) {
            throw new RuntimeException("Invitation not found for user");
        }
        invitation.setStatus(newStatus);
        invitationRepository.save(invitation);
    }
}

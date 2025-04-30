package com.example.demo.javaSrc.eventsANDtask;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByUserId(Long userId);

    Invitation findByInvitationIdAndUserId(Long invitationId, Long userId);
}

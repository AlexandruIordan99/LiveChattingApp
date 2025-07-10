package com.example.LiveChattingApp.ChatParticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, String> {

  @Query("""
        select cp from ChatParticipant cp
        where cp.chat.id = :chatId
        and cp.isActive = true
    """)
  List<ChatParticipant> findActiveParticipantsByChatId(@Param("chatId") String chatId);

  @Query("""
        select cp from ChatParticipant cp
        where cp.chat.id = :chatId
        and cp.user.id = :userId
        and cp.isActive = true
    """)
  Optional<ChatParticipant> findByChatIdAndUserId(@Param("chatId") String chatId,
                                                  @Param("userId") String userId);

  @Query("""
        select cp from ChatParticipant cp
        where cp.chat.id = :chatId
        and cp.role = com.example.LiveChattingApp.chat.ParticipantRole.ADMIN
        and cp.isActive = true
    """)
  List<ChatParticipant> findAdminsByChatId(@Param("chatId") String chatId);

}


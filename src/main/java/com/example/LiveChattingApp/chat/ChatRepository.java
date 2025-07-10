package com.example.LiveChattingApp.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {

  @Query("""
         select chat from Chat chat
                join chat.participants participants
                where participants.user.id = :userId
                and participants.isActive = true
                order by chat.lastModifiedDate desc
""")
  List<Chat> findChatsByUserId(@Param("senderId") String userId);

  @Query("""
        select chat from Chat chat
        where chat.type = com.example.LiveChattingApp.chat.ChatType.DIRECT
        and exists (
            select 1 from ChatParticipant cp1 
            where cp1.chat = chat and cp1.user.id = :user1Id and cp1.isActive = true
        )
        and exists (
            select 1 from ChatParticipant cp2 
            where cp2.chat = chat and cp2.user.id = :user2Id and cp2.isActive = true
        )
        and (
            select count(cp3) from ChatParticipant cp3 
            where cp3.chat = chat and cp3.isActive = true
        ) = 2
    """)
  Optional<Chat> findDirectChatBetweenUsers(@Param("user1Id") String user1Id,
                                            @Param("user2Id") String user2Id);


  @Query("""
        select chat from Chat chat
        join chat.participants participants
        where participants.user.id = :userId
        and participants.isActive = true
        and chat.type = com.example.LiveChattingApp.chat.ChatType.GROUP
        order by chat.lastModifiedDate desc
    """)
  List<Chat> findGroupChatsByUserId(@Param("userId") String userId);



}

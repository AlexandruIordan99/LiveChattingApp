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
                where participants.id = :userId
                order by chat.lastModifiedDate desc
""")
  List<Chat> findChatsByUserId(@Param("senderId") String userId);

  @Query("""
       select chat from Chat chat
               where chat.type = com.example.LiveChattingApp.chat.ChatType.DIRECT
               and :user1Id member of chat.participants
               and :user2Id member of chat.participants
               and size(chat.participants) = 2
    """)
  Optional<Chat> findDirectChatBetweenUsers(@Param("user1Id") String user1Id,
                                            @Param("user2Id") String user2Id);


  @Query("""
         select chat from Chat chat
                join chat.participants participants
                where participants.id = :userId
                and chat.type = com.example.LiveChattingApp.chat.ChatType.GROUP
                order by chat.lastModifiedDate desc
    """)
  List<Chat> findGroupChatsByUserId(@Param("userId") String userId);



}

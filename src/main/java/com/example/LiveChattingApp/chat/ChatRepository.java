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
  List<Chat> findChatsByUserId(@Param("userId") String userId);

  @Query("""
       select chat from Chat chat
       where chat.type = com.example.LiveChattingApp.chat.ChatType.DIRECT
       and size(chat.participants) = 2
       and exists (select 1 from chat.participants p1 where p1.id = :user1Id)
       and exists (select 1 from chat.participants p2 where p2.id = :user2Id)
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

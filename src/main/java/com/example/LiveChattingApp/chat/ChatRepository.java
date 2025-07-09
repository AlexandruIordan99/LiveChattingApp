package com.example.LiveChattingApp.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

  @Query("""
        select chat from Chat chat
        join chat.participants participants
        where participants.id= :senderId
""")
  List<Chat> findChatsBySenderId(@Param("senderId") String senderId);

  @Query("""
    select chat from Chat chat
    where chat.type =  com.example.LiveChattingApp.chat.ChatType.DIRECT
    and ((chat.sender.id = :user1Id and chat.receiver.id = :user2Id)
    or (chat.sender.id=:user2Id and chat.receiver.id= :user1Id))
""")
  Optional<Chat> findDirectChatBetweenUsers(@Param("user1Id") String user1Id,
                                            @Param("user2Id") String user2Id);

}

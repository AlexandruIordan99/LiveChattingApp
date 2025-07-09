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
        where participants.id= :userId
""")
  List<Chat> findChatsByUserId(@Param("userId") Integer userId);

  @Query("""
    select chat from Chat chat
    where chat.type = 'DIRECT'
    and ((chat.sender.id = :user1Id and chat.receiver.id = :user2Id)
    or (chat.sender.id=:user2Id and chat.receiver.id= :user1Id))
""")
  Optional<Chat> findDirectChatBetweenUsers(@Param("user1Id") Integer user1Id,
                                            @Param("user2Id") Integer user2Id);

}

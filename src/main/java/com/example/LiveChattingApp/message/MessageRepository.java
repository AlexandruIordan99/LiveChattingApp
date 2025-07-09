package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, String> {

  @Query("""
    select message from Message message\s
    where message.chat.id = :chatId
    order by message.createdDate
""")
  Optional<Message> findMessageByChatId(@Param("chatId") String chatId);

  @Transactional
  @Modifying
  @Query("""
    update Message message
    set message.state = :state
    where message.chat.id = :chatId
    
""")
  void setMessagesToSeen(@Param("chatId") Chat chat,
                         @Param(" state") MessageState state);

}

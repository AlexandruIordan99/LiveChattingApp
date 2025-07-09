package com.example.LiveChattingApp.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {

  @Query("""
    select message from Message message\s
    where message.chat.id = :chatId
    order by message.createdDate
""")
  Optional<Message> findMessageByChatId(@Param("chatId") Integer chatId);

}

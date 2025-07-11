package com.example.LiveChattingApp.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {

  @Query("""
        select message from Message message 
        where message.chat.id = :chatId
        order by message.createdDate
    """)
  List<Message> findMessagesByChatId(@Param("chatId") String chatId);

  @Query("""
        select count(message) from Message message
        where message.chat.id = :chatId
        and message.sender.id != :userId
        and message.state != com.example.LiveChattingApp.message.MessageState.READ
    """)
  long countUnreadMessagesByChatIdAndUserId(@Param("chatId") String chatId,
                                            @Param("userId") String userId);


  @Query("""
        select message from Message message
        where message.id = :messageId
        and message.state = com.example.LiveChattingApp.message.MessageState.READ
    """)
  List<Message> findReadMessagesByMessageId(@Param("messageId") String messageId);

  @Transactional
  @Modifying
  @Query("""
        update Message message
        set message.state = com.example.LiveChattingApp.message.MessageState.READ
        where message.chat.id = :chatId
        and message.sender.id != :userId
        and message.state != com.example.LiveChattingApp.message.MessageState.READ
    """)
  void markMessagesAsRead(@Param("chatId") String chatId,
                          @Param("userId") String userId);

}


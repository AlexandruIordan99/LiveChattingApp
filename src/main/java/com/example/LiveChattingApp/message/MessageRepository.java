package com.example.LiveChattingApp.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

  @Query("""
        select message from Message message\s
        where message.chat.id = :chatId
        order by message.createdDate
   \s""")
  List<Message> findMessagesByChatId(@Param("chatId") Long chatId);

  @Query("""
        select count(message) from Message message
        where message.chat.id = :chatId
        and message.sender.id != :userId
        and message.state != com.example.LiveChattingApp.message.MessageState.READ
    """)
  long countUnreadMessagesByChatIdAndUserId(@Param("chatId") Long chatId,
                                            @Param("userId") String userId);


  @Query("""
        select message from Message message
        where message.id = :messageId
        and message.state = com.example.LiveChattingApp.message.MessageState.READ
    """)
  List<Message> findReadMessagesByMessageId(@Param("messageId") Long messageId);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("""
        update Message message
        set message.state = :readState
        where message.chat.id = :chatId
        and message.sender.id != :userId
        and message.state != :readState
    """)
  void markMessagesAsRead(@Param("chatId") Long chatId,
                          @Param("userId") String userId,
                          @Param("readState") MessageState readState);
                          //need to pass the state explicitly in the service


}


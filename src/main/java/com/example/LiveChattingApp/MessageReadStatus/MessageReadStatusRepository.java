package com.example.LiveChattingApp.MessageReadStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, String> {

  @Query("""
        select mrs from MessageReadStatus mrs
        where mrs.message.id = :messageId
        and mrs.state = com.example.LiveChattingApp.message.MessageState.READ
    """)

  List<MessageReadStatus> findReadStatusesByMessageId(@Param("messageId") String messageId);

  @Transactional
  @Modifying
  @Query("""
        update MessageReadStatus mrs
        set mrs.state = com.example.LiveChattingApp.message.MessageState.READ,
            mrs.readAt = :readAt
        where mrs.message.chat.id = :chatId
        and mrs.user.id = :userId
        and mrs.state != com.example.LiveChattingApp.message.MessageState.READ
    """)

  void markMessagesAsRead(@Param("chatId") String chatId,
                          @Param("userId") String userId,
                          @Param("readAt") LocalDateTime readAt);


}

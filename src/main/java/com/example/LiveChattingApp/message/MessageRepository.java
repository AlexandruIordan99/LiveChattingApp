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

  @Query("""
        select count(message) from Message message
        where message.chat.id = :chatId
        and message.sender.id != :userId
        and not exists (
            select 1 from MessageReadStatus mrs
            where mrs.message = message
            and mrs.user.id = :userId
            and mrs.state = com.example.LiveChattingApp.message.MessageState.READ
        )
    """)

  long countUnreadMessagesByChatIdAndUserId(@Param("chatId") String chatId,
                                            @Param("userId") String userId);


}

package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class Chat extends BaseAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "chat_participants",
    joinColumns = @JoinColumn(name = "chat_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> participants;

  @Transient
  public String getChatName(final Integer senderId){
    if(receiver.getId().equals(senderId)){
      return sender.getDisplayName();
    }
    return receiver.getDisplayName();
  }

  @Transient
  public Long getUnreadMessagesCount(){
    return messages.stream()
      .filter(message -> message.getReceiver().getId().equals(sender.getId()))
      .filter(message -> message.getState()  ==  MessageState.SENT)
      .count();
  }

  @Transient
  public String getLastMessage() {
    if (messages != null && !messages.isEmpty()) {
      if (messages.getFirst().getType() != MessageType.TEXT) {
        return "Attachment - to be implemented";
      }
      return messages.getFirst().getContent();
    }
    return null;
  }

  @Transient
  public LocalDateTime getLastMessageTime() {
    if (messages != null && !messages.isEmpty()) {
      return messages.getFirst().getCreatedDate();
    }
    return null;
  }

  @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
  @OrderBy("createdDate DESC")
  private List<Message> messages;

}

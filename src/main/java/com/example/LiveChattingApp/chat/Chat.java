package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageState;
import com.example.LiveChattingApp.message.MessageType;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
public class Chat extends BaseAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  private User receiver;

  private String name;

  @Enumerated
  private ChatType type;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "chat_participants",
    joinColumns = @JoinColumn(name = "chat_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private List<User> participants;

  public void setChatName(final String chatName){
    if(type == ChatType.GROUP){
      this.name = chatName;
    }
  }

  @Transient
  public String getChatName(final Integer userId){
    if(type == ChatType.DIRECT){
      return participants.stream()
        .filter(p -> !p.getId().equals(userId))
        .findFirst()
        .map(User::getDisplayName)
        .orElse("Unknown");
    }
    return name !=null ? name: generateDefaultGroupName();
  }

  @Transient
  private String generateDefaultGroupName() {
    if (participants.size() <= 3){
      return participants.stream()
        .map(User::getDisplayName)
        .collect(Collectors.joining(", "));
    }
    return participants.stream()
      .limit(2)
      .map(User::getDisplayName)
      .collect(Collectors.joining(", ")) + " and " + (participants.size() - 2) + " others";
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
  @OrderBy("createdDate DESC")
  private List<Message> messages;

  @Transient
  public Long getUnreadMessagesCount(Integer userId){
    return messages.stream()
      .filter(message -> message.getReceiver().getId().equals(userId))
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

}

package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageState;
import com.example.LiveChattingApp.message.MessageType;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.*;

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
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id")
  private User creator;
  private String name;

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
    name = "chat_participants",
    joinColumns = @JoinColumn(name = "chat_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> participants = new HashSet<>();

  @ElementCollection
  private Set<Long> adminUserIds = new HashSet<>(); // Simple set of admin IDs

  @Enumerated
  private ChatType type;

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
  private List<MessageRequest> messageRequests;

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
  private List<Message> messages;

  private LocalDateTime lastModifiedDate;

  public String getChatName(Long userId) {
    if (type == ChatType.GROUP) {
      return name != null ? name : "Group Chat";
    } else {
      return participants.stream()
        .filter(participant -> !participant.getId().equals(userId))
        .map(User::getDisplayName)
        .findFirst()
        .orElse("Unknown");
    }
  }

  public boolean isParticipant(Long userId) {
    return participants.stream()
      .anyMatch(p -> p.getId().equals(userId));
  }

  public boolean isParticipant(User user){
    return participants.stream()
      .anyMatch(p -> p.equals(user));
  }


  public User getOtherParticipant(Long userId) {
    if (type != ChatType.DIRECT) {
      throw new IllegalStateException("This method is only for direct chats");
    }
    return participants.stream()
      .filter(user -> !user.getId().equals(userId))
      .findFirst()
      .orElse(null);
  }

  @Transient
  public Long getUnreadMessagesCount(Long  userId){
    return messages.stream()
      .filter(message -> message.getSender().getId().equals(userId))
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

  public boolean isAdmin(Long userId) {
    return adminUserIds.contains(userId) || creator.getId().equals(userId);
  }

  @ElementCollection
  @MapKeyColumn(name = "user_id")
  @Column(name = "last_read_at")
  private Map<Long, LocalDateTime> lastReadTimestamps = new HashMap<>();


  @PrePersist
  @PreUpdate
  private void validateChatType(){
    if(type == ChatType.DIRECT && participants != null && participants.size() != 2){
      throw new IllegalArgumentException("Direct chats must have exactly 2 participants.");
    }
    if(type == ChatType.GROUP && participants != null && participants.size() < 3){
      throw new IllegalArgumentException("Group chats must have at least 3 participants.");
    }
  }

}

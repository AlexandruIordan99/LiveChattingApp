package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatus;
import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "message")
public class Message extends BaseAuditingEntity {

  @Id
  @SequenceGenerator(name = "msg_seq", sequenceName = "msg_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "msg_seq")
  private String id;
  @Column(columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch =FetchType.LAZY)
  @JoinColumn(name ="sender_id", nullable = false)
  private User sender;

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
  private Set<MessageReadStatus> readStatuses;

  @ManyToOne
  @JoinColumn(name = "reply_to_id")
  private Message replyTo;

  @OneToMany(mappedBy = "replyTo")
  private Set<Message> replies;

  private MessageState state;
  private MessageType type;

  @ManyToOne
  @JoinColumn(name="chat_id")
  private Chat chat;

  private String mediaFilePath;

}

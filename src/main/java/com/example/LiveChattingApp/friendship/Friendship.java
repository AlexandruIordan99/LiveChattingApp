package com.example.LiveChattingApp.friendship;


import com.example.LiveChattingApp.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "friendship",
  uniqueConstraints = @UniqueConstraint(columnNames = {"users_id", "friend_id"}
))
public class Friendship {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "users_id")
  private User user;

  @ManyToOne
  @JoinColumn(name="friend_id")
  private User friend;

  @Enumerated(EnumType.STRING)
  private FriendshipStatus friendshipsStatus;

  @CreationTimestamp
  private LocalDateTime createdAt;

}

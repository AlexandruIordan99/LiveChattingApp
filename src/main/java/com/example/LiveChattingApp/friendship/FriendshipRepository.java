package com.example.LiveChattingApp.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FriendshipRepository extends JpaRepository {

  @Query("""
    Select f from Friendship f
    where (f.user.id = :userId and f.friend.id= :friendId)\s
    or (f.user.id = :friendId and f.friend.id =:userId)
   \s""")
  Optional<Friendship> friendshipBetweenUsers(@Param("userId") Integer userId,
                                              @Param("friendId") Integer friendId);
}

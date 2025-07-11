package com.example.LiveChattingApp.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, String > {

  @Query("""
    Select f from Friendship f
    where (f.user.id = :userId and f.friend.id= :friendId)\s
    or (f.user.id = :friendId and f.friend.id =:userId)
   \s""")
  Optional<Friendship> findFriendshipBetweenUsers(@Param("userId") String  userId,
                                              @Param("friendId") String  friendId);

  @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END\s
        FROM Friendship f\s
        WHERE (f.user.id = :userId AND f.friend.id = :friendId)\s
           OR (f.user.id = :friendId AND f.friend.id = :userId)
       \s""")
  boolean existsFriendshipBetweenUsers(@Param("userId") String  userId,
                                       @Param("friendId") String  friendId);

  @Query("""
    select f from Friendship f
    where (f.user.id= :userId or f.friend.id= :userId)
    and f.friendshipsStatus = 'ACCEPTED'
""")
  List<Friendship> findAcceptedFriendships(@Param("userId") String userId);

  @Query("""
    select f from Friendship f
    where f.user.id =:userId
    and f.friendshipsStatus = 'PENDING'
""")
    List<Friendship> findPendingSentRequests(@Param("userId") String  userId);


  @Query("""
        SELECT f FROM Friendship f
        WHERE f.friend.id = :userId\s
          AND f.friendshipsStatus = 'PENDING'
       \s""")
  List<Friendship> findPendingReceivedRequests(@Param("userId") String  userId);

  @Query("""
        SELECT f FROM Friendship f
        WHERE f.user.id = :userId\s
         AND f.friendshipsStatus = 'BLOCKED'
       \s""")
  List<Friendship> findBlockedUsers(@Param("userId") String userId);

  List<Friendship> findByUserIdAndFriendshipsStatus(String  userId, FriendshipStatus status);

  List<Friendship> findByFriendIdAndFriendshipsStatus(String friendId, FriendshipStatus status);

}

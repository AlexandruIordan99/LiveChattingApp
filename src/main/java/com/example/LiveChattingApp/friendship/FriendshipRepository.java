package com.example.LiveChattingApp.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

  @Query("""
    Select f from Friendship f
    where (f.user.id = :usersId and f.friend.id= :friendId)\s
    or (f.user.id = :friendId and f.friend.id =:usersId)
   \s""")
  Optional<Friendship> findFriendshipBetweenUsers(@Param("usersId") Integer usersId,
                                              @Param("friendId") Integer friendId);

  @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END 
        FROM Friendship f 
        WHERE (f.user.id = :userId AND f.friend.id = :friendId) 
           OR (f.user.id = :friendId AND f.friend.id = :userId)
        """)
  boolean existsFriendshipBetweenUsers(@Param("usersId") Integer usersId,
                                       @Param("friendId") Integer friendId);

  @Query("""
    select f from Friendship f
    where (f.user.id= :usersId and f.friend.id= :friendId)
    and f.friendshipsStatus = 'ACCEPTED'
""")
  List<Friendship> findAcceptedFriendships(@Param("usersId") Integer usersId);

  @Query("""
    select f from Friendship f
    where f.user.id =:usersId
    and f.friendshipsStatus = 'PENDING'
""")
    List<Friendship> findPendingSentFriendships(@Param("usersId") Integer usersId);


  @Query("""
        SELECT f FROM Friendship f
        WHERE f.friend.id = :userId 
          AND f.friendshipsStatus = 'PENDING'
        """)
  List<Friendship> findPendingReceivedRequests(@Param("usersId") Integer userId);

  @Query("""
        SELECT f FROM Friendship f
        WHERE f.user.id = :userId 
         AND f.friendshipsStatus = 'BLOCKED'
        """)
  List<Friendship> findBlockedUsers(@Param("usersId") Integer usersId);

  @Query("""
    select case when count(f) > 0 then true else false end from Friendship f where
    (f.user.id= :userId and f.friend.id = :friendId) or
    (f.user.id= :friendId and f.friend.id = :userId)
""")
  List<Friendship> findByUserIdAndFriendshipsStatus(Integer userId, FriendshipStatus status);

  List<Friendship> findByFriendIdAndFriendshipsStatus(Integer friendId, FriendshipStatus status);;

}

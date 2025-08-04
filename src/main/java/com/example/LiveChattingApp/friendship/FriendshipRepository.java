package com.example.LiveChattingApp.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long > {

  @Query("""
    select f from Friendship f
    where (f.user.id = :userId and f.friend.id= :friendId)\s
    or (f.user.id = :friendId and f.friend.id =:userId)
   \s""")
  Optional<Friendship> findFriendshipBetweenUsers(@Param("userId") Long  userId,
                                              @Param("friendId") Long  friendId);

  @Query("""
        select count(f) > 0 \s
       from Friendship f\s
        where (f.user.id = :userId and f.friend.id = :friendId)\s
           or (f.user.id = :friendId and f.friend.id = :userId)
       and f.friendshipsStatus = 'ACCEPTED'
       \s""")
  boolean existsFriendshipBetweenUsers(@Param("userId") Long  userId,
                                       @Param("friendId") Long  friendId);

  @Query("""
    select f from Friendship f
    where (f.user.id= :userId or f.friend.id= :userId)
    and f.friendshipsStatus = 'ACCEPTED'
""")
  List<Friendship> findAcceptedFriendships(@Param("userId") Long userId);

  @Query("""
    select f from Friendship f
    where f.user.id =:userId
    and f.friendshipsStatus = 'PENDING'
""")
    List<Friendship> findPendingSentRequests(@Param("userId") Long userId);


  @Query("""
       select f from Friendship f
        where f.friend.id = :userId\s
          and f.friendshipsStatus = 'PENDING'
       \s""")
  List<Friendship> findPendingReceivedRequests(@Param("userId") Long  userId);

  @Query("""
        select f from Friendship f
        where f.user.id = :userId\s
         and f.friendshipsStatus = 'BLOCKED'
       \s""")
  List<Friendship> findBlockedUsers(@Param("userId") Long userId);

  List<Friendship> findByUserIdAndFriendshipsStatus(Long userId, FriendshipStatus status);

  List<Friendship> findByFriendIdAndFriendshipsStatus(Long friendId, FriendshipStatus status);

}

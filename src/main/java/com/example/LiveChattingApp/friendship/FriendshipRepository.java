package com.example.LiveChattingApp.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String > {

  @Query("""
    select f from Friendship f
    where (f.user.id = :userId and f.friend.id= :friendId)\s
    or (f.user.id = :friendId and f.friend.id =:userId)
   \s""")
  Optional<Friendship> findFriendshipBetweenUsers(@Param("userId") String  userId,
                                              @Param("friendId") String  friendId);

  @Query("""
        select count(f) > 0 \s
       from Friendship f\s
        where (f.user.id = :userId and f.friend.id = :friendId)\s
           or (f.user.id = :friendId and f.friend.id = :userId)
       and f.friendshipsStatus = :status
       \s""")
  boolean existsFriendshipBetweenUsers(@Param("userId") String  userId,
                                       @Param("friendId") String  friendId,
                                        @Param("friendshipStatus") FriendshipStatus status);

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
       select f from Friendship f
        where f.friend.id = :userId\s
          and f.friendshipsStatus = 'PENDING'
       \s""")
  List<Friendship> findPendingReceivedRequests(@Param("userId") String  userId);

  @Query("""
        select f from Friendship f
        where f.user.id = :userId\s
         and f.friendshipsStatus = 'BLOCKED'
       \s""")
  List<Friendship> findBlockedUsers(@Param("userId") String userId);

  List<Friendship> findByUserIdAndFriendshipsStatus(String  userId, FriendshipStatus status);

  List<Friendship> findByFriendIdAndFriendshipsStatus(String friendId, FriendshipStatus status);

}

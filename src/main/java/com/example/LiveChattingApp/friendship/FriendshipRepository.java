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
  Optional<Friendship> friendshipBetweenUsers(@Param("usersId") Integer usersId,
                                              @Param("friendId") Integer friendId);

  boolean existsFriendshipBetweenUsers(@Param("usersId") Integer usersId,
                                       @Param("friendId") Integer friendId);

  @Query("""
    select f from Friendship f
    where (f.user.id= :usersId and f.friend.id= :friendId)
    and f.friendshipsStatus = 'ACCEPTED'\s
""")
  List<Friendship> findAcceptedFriendships(@Param("usersId") Integer usersId,
                                           @Param("friendId") Integer friendId);

  @Query("""
    select f from Friendship f\s
    where(f.user.id =:usersId and f.friend.id= :friendId)
    and f.friendshipsStatus = 'PENDING'
""")
    List<Friendship> findPendingFriendships(@Param("usersId") Integer usersId,
                                            @Param("friendId") Integer friendId);

    @Query("""
    select f from Friendship f\s
    where(f.user.id =:usersId and f.friend.id= :friendId)
    and f.friendshipsStatus = 'BLOCKED'
""")
  List<Friendship> findBlockedUsers(@Param("usersId") Integer usersId,
                                    @Param("friendId") Integer friendId);

  @Query("""
    select case when count(f) > 0 then true else false end from Friendship f where
    (f.user.id= :userId and f.friend.id = :friendId) or
    (f.user.id= :friendId and f.friend.id = :userId)
""")

  List<Friendship> findRequestByUserIdAndStatus(Integer userId, FriendshipStatus status);

  List<Friendship> findRequestByFriendIdAndStatus(Integer friendId, FriendshipStatus status);

}

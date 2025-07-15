package com.example.LiveChattingApp.friendship;

import com.example.LiveChattingApp.friendship.exceptions.FriendshipAlreadyExistsException;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipNotFoundException;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

  private final FriendshipRepository friendshipRepository;
  private final UserRepository userRepository;

  public  FriendshipResponseDTO sendFriendRequest(String  currentUserId, String  friendId){
    User currentUser = findUserById(currentUserId);
    User friendUser = findUserById(friendId);

    if(currentUserId.equals(friendId)){
      throw new IllegalArgumentException("A user cannot send a friend request to themselves.");
    }

    if(friendshipRepository.existsFriendshipBetweenUsers(currentUserId, friendId)){
      throw new FriendshipAlreadyExistsException("Friendship already exists between these users.");
    }

    Friendship friendship = new Friendship();
    friendship.setUser(currentUser);
    friendship.setFriend(friendUser);

    friendship.setFriendshipsStatus(FriendshipStatus.PENDING);
    Friendship savedFriendship = friendshipRepository.save(friendship);

    return mapToResponseDto(savedFriendship, currentUserId);
  }


  public  FriendshipResponseDTO acceptFriendRequest(String  currentUserId, String  friendshipId){
    Friendship friendship = findFriendshipById(friendshipId);

    if(!friendship.getFriend().getId().equals(currentUserId)){
      throw new IllegalArgumentException("You can only accept requests sent to you.");
    }

    if(friendship.getFriendshipsStatus()!= FriendshipStatus.PENDING){
      throw new IllegalStateException("You can only accept pending requests.");
    }

    friendship.setFriendshipsStatus(FriendshipStatus.ACCEPTED);
    Friendship savedFriendship = friendshipRepository.save(friendship);
    return mapToResponseDto(savedFriendship, currentUserId);
  }

  public void rejectFriendRequest(String  currentUserId, String  friendshipId){
    Friendship friendship = findFriendshipById(friendshipId);

    if(!friendship.getFriend().getId().equals(currentUserId)){
      throw new IllegalArgumentException("You can only reject requests sent to you.");
    }
    if(friendship.getFriendshipsStatus()!= FriendshipStatus.PENDING){
      throw new IllegalStateException("You can only reject pending requests.");
    }

    friendship.setFriendshipsStatus(FriendshipStatus.REJECTED);

    friendshipRepository.save(friendship);
  }

  public  void removeFriend(String  currentUserId, String friendId){
    Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUserId, friendId)
      .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found between these users"));

    friendshipRepository.delete(friendship);
  }

  public void blockUser(String  currentUserId, String  userToBlockId){
    User userToBlock = findUserById(userToBlockId);

    Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUserId, userToBlockId)
      .orElse(new Friendship());

    friendship.setFriend(userToBlock);
    friendship.setFriendshipsStatus(FriendshipStatus.BLOCKED);

    friendshipRepository.save(friendship);
  }

  @Transactional(readOnly= true)
  public List<FriendshipResponseDTO> getFriends(String  userId){

    List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);

    return friendships.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly= true)
  public List<FriendshipResponseDTO> getReceivedPendingRequests(String  userId){
    List<Friendship> pendingFriendships = friendshipRepository.findPendingReceivedRequests(userId);

    return pendingFriendships.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly= true)
  public List<FriendshipResponseDTO> getSentPendingRequests(String userId){
    List<Friendship> pendingFriendships = friendshipRepository.findPendingSentRequests(userId);

    return pendingFriendships.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public List<FriendshipResponseDTO> getBlockedUsers(String userId){
    List<Friendship> blockedUsers=  friendshipRepository.findBlockedUsers(userId);

    return blockedUsers.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public String getFriendshipStatus(String  userId, String friendId){
    return friendshipRepository.findFriendshipBetweenUsers(userId, friendId)
      .map(friendship -> friendship.getFriendshipsStatus().toString())
      .orElse("NONE");
  }

  public boolean existsFriendshipBetweenUsers(String userId, String friendId){
    return friendshipRepository.existsFriendshipBetweenUsers(userId, friendId);
  }

  private User findUserById(String userId){
    return userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
  }

  private Friendship findFriendshipById(String friendshipId){
    return friendshipRepository.findById(friendshipId)
      .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found with ID: " + friendshipId));
  }

  private FriendshipResponseDTO mapToResponseDto(Friendship friendship, String currentUserId){
    User otherUser = friendship.getUser().getId().equals(currentUserId)
      ? friendship.getFriend() : friendship.getUser();

   boolean isRequester = friendship.getUser().getId().equals(currentUserId);

   FriendshipResponseDTO dto = FriendshipResponseDTO
     .builder()
     .id(friendship.getId())
     .userId(otherUser.getId())
     .displayName(otherUser.getDisplayName())
     .email(otherUser.getEmail())
     .status(friendship.getFriendshipsStatus().toString())
     .createdAt(friendship.getCreatedAt().toString())
     .isRequester(isRequester)
     .build();

   return dto;
  }



}

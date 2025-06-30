package com.example.LiveChattingApp.friendship;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
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

  private FriendshipResponseDTO sendFriendRequest(Integer currentUserId, Integer friendId){
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


  private FriendshipResponseDTO acceptFriendRequest(Integer currentUserId, Integer friendshipId){
    User currentUser = findUserById(currentUserId);
    Friendship friendship = findFriendshipById(friendshipId);
    friendship.setUser(currentUser);

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

  private FriendshipResponseDTO rejectFriendRequest(Integer currentUserId, Integer friendshipId){
    User currentUser = findUserById(currentUserId);
    Friendship friendship = findFriendshipById(friendshipId);
    friendship.setUser(currentUser);

    if(!friendship.getFriend().getId().equals(currentUserId)){
      throw new IllegalArgumentException("You can only reject requests sent to you.");
    }
    if(friendship.getFriendshipsStatus()!= FriendshipStatus.PENDING){
      throw new IllegalStateException("You can only reject pending requests.");
    }

    friendship.setFriendshipsStatus(FriendshipStatus.REJECTED);
    Friendship savedFriendship = friendshipRepository.save(friendship);

    return mapToResponseDto(savedFriendship, currentUserId);
  }

  private void removeFriend(Integer currentUserId, Integer friendId){
    boolean exists = friendshipRepository.existsFriendshipBetweenUsers(currentUserId, friendId);

    if(!exists){
      throw new FriendshipNotFoundException("You can only remove a friend you have a friendship with.");
    }

    Friendship friendship = findFriendshipById(currentUserId);
    User currentUser = findUserById(currentUserId);
    friendship.setUser(currentUser);

    friendshipRepository.delete(friendship);
  }

  private void blockUser(Integer currentUserId, Integer userToBlockId){
    User currentUser = findUserById(currentUserId);
    User userToBlock = findUserById(userToBlockId);

    Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUserId, userToBlockId)
      .orElse(new Friendship());

    friendship.setUser(currentUser);
    friendship.setFriend(userToBlock);
    friendship.setFriendshipsStatus(FriendshipStatus.BLOCKED);

    friendshipRepository.save(friendship);
  }

  @Transactional(readOnly= true)
  public List<FriendshipResponseDTO> getFriends(Integer userId){

    List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);

    return friendships.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }

  @Transactional(readOnly= true)
  public List<FriendshipResponseDTO> getPendingRequests(Integer userId){
    List<Friendship> pendingFriendships = friendshipRepository.findPendingFriendships(userId);

    return pendingFriendships.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public List<FriendshipResponseDTO> getBlockedUsers(Integer userId){
    List<Friendship> blockedUsers=  friendshipRepository.findBlockedUsers(userId);

    return blockedUsers.stream()
      .map(friendship -> mapToResponseDto(friendship, userId))
      .collect(Collectors.toList());
  }



  private User findUserById(Integer userId){
    return userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
  }

  private Friendship findFriendshipById(Integer friendshipId){
    return friendshipRepository.findById(friendshipId)
      .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found with ID: " + friendshipId));
  }

  private FriendshipResponseDTO mapToResponseDto(Friendship friendship, Integer currentUserId){
    User otherUser = friendship.getUser().getId().equals(currentUserId)
      ? friendship.getFriend() : friendship.getUser();

   boolean isRequester = friendship.getUser().getId().equals(currentUserId);

   FriendshipResponseDTO dto = FriendshipResponseDTO
     .builder()
     .id(friendship.getId())
     .userId(otherUser.getId())
     .username(otherUser.getUsername())
     .email(otherUser.getEmail())
     .status(friendship.getFriendshipsStatus().toString())
     .createdAt(friendship.getCreatedAt().toString())
     .isRequester(isRequester)
     .build();

   return dto;
  }



}

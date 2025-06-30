package com.example.LiveChattingApp.friendship;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipAlreadyExistsException;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipNotFoundException;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
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

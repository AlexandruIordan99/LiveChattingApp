package com.example.LiveChattingApp.friendship;


import com.example.LiveChattingApp.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/friendship")
@RequiredArgsConstructor
@Tag(name="Friendship")
public class FriendshipController {

  private final FriendshipService service;

  @PostMapping("/request")
  public ResponseEntity<FriendshipResponseDTO> sendFriendRequest(
    @Valid Authentication authentication,
    @Valid @RequestBody FriendshipRequestDTO friendDTO){
    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    return ResponseEntity.ok(service.sendFriendRequest(userId, friendDTO.getFriendId()));
  }

  @PutMapping("{friendshipId}/accept")
  public ResponseEntity<?> acceptFriendRequest(
    @Valid Authentication authentication,
    @PathVariable String friendshipId){

    User user = (User) authentication.getPrincipal();
    String userId = user.getId();
    FriendshipResponseDTO response = service.acceptFriendRequest(userId, friendshipId);

    return  ResponseEntity.ok(response);

  }

  @PutMapping("{friendshipId}/reject")
  public ResponseEntity<Void> rejectFriendRequest
    (@Valid Authentication authentication,
     @PathVariable String  friendshipId) {

    User user = (User) authentication.getPrincipal();
    String userId = user.getId();

    service.rejectFriendRequest(userId, friendshipId);
    return ResponseEntity.ok().build();
  }


  @DeleteMapping("friends/{friendId}")
  public ResponseEntity<Void> removeFriend
    (@Valid Authentication authentication,
     @PathVariable String  friendId){

    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    service.removeFriend(userId, friendId);

    return ResponseEntity.ok().build();

  }

  @PostMapping("/block/{userToBlockId}")
  public ResponseEntity<Void> blockUser(@Valid Authentication authentication,
  @PathVariable String  userToBlockId){
    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    service.blockUser(userId, userToBlockId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/friendslist")
  public ResponseEntity<List<FriendshipResponseDTO>> getFriends(
    @Valid Authentication authentication){
    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    List<FriendshipResponseDTO> response = service.getFriends(userId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/sent-pending-requests")
  public ResponseEntity<List<FriendshipResponseDTO>> getSentPendingRequests (
    @Valid Authentication authentication){
    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    List<FriendshipResponseDTO> response = service.getSentPendingRequests(userId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/received-pending-requests")
  public ResponseEntity<List<FriendshipResponseDTO>> getReceivedPendingRequests(
    @Valid Authentication authentication){
    User user = (User) authentication.getPrincipal();
    String  userId  = user.getId();

    List<FriendshipResponseDTO> response = service.getReceivedPendingRequests(userId);

    return ResponseEntity.ok(response);

  }

  @GetMapping("/blocked-users")
  public ResponseEntity<List<FriendshipResponseDTO>> getBlockedUsers(
    @Valid Authentication authentication){
    User user = (User) authentication.getPrincipal();
    String userId = user.getId();

    List<FriendshipResponseDTO> response = service.getBlockedUsers(userId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/friendship-status/{friendId}")
  public ResponseEntity<String> getFriendshipStatus(
    @Valid Authentication authentication,
    @PathVariable String  friendId){
    User user = (User) authentication.getPrincipal();
    String  userId = user.getId();

    String response = String.valueOf(FriendshipStatus.valueOf(
      service.getFriendshipStatus(userId, friendId)
    ));

    return ResponseEntity.ok(response);
  }

}

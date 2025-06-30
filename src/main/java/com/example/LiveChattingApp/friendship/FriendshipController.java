package com.example.LiveChattingApp.friendship;


import com.example.LiveChattingApp.friendship.DTOs.FriendshipRequestDTO;
import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
import com.example.LiveChattingApp.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendshipController {

  private final FriendshipService service;


  @PostMapping("/request")
  public ResponseEntity<FriendshipResponseDTO> sendFriendRequest(
    @Valid Authentication authentication,
    @RequestBody FriendshipRequestDTO friendDTO){
    User user = (User) authentication.getPrincipal();
    Integer userId = user.getId();

    return ResponseEntity.ok(service.sendFriendRequest(userId, friendDTO.getFriendId()));
  }

  @PutMapping("{friendshipId}/accept")
  public ResponseEntity<?> acceptFriendRequest(
    @Valid Authentication authentication,
    @PathVariable Integer friendshipId){

    User user = (User) authentication.getPrincipal();
    Integer userId = user.getId();
    FriendshipResponseDTO response = service.acceptFriendRequest(userId, friendshipId);

    return  ResponseEntity.ok(response);

  }


  @PutMapping("{friendshipId}/reject")
  public ResponseEntity<Void> rejectFriendRequest
    (@Valid Authentication authentication,
     @PathVariable Integer friendshipId) {

    User user = (User) authentication.getPrincipal();
    Integer userId = user.getId();

     service.rejectFriendRequest(userId, friendshipId);
    return ResponseEntity.ok().build();
  }


  @DeleteMapping("friends/{friendId}")
  public ResponseEntity<Void> removeFriend
    (@Valid Authentication authentication,
     @PathVariable Integer friendId){

    User user = (User) authentication.getPrincipal();
    Integer userId = user.getId();

    service.removeFriend(userId, friendId);

    return ResponseEntity.ok().build();

  }


  @PostMapping("block/{userToBlockId}")
  public ResponseEntity<Void> blockUser(@Valid Authentication authentication,
  @PathVariable Integer userToBlockId){
    User user = (User) authentication.getPrincipal();
    Integer userId = user.getId();

    service.blockUser(userId, userToBlockId);
    return ResponseEntity.ok().build();
  }


}

package com.example.LiveChattingApp.chat;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserDTO;
import com.example.LiveChattingApp.user.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chats")
@RestController
@Tag(name="Chat")
public class ChatController {

  private final ChatService chatService;
  private final UserRepository userRepository;

  @PostMapping("/direct")
  public ResponseEntity<Long> createDirectChat(
    Authentication authentication,
    @RequestBody UserDTO receiverDTO
   ) {
    Long senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    Long chatId = chatService.createDirectChat(senderId, receiverDTO.getId());
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/group")
  public ResponseEntity<Long> createGroupChat(
    Authentication authentication,
    @RequestBody CreateGroupChatRequest request) {
    Long senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    Long chatId = chatService.createGroupChat(
      senderId,
      request.getName(),
      request.getParticipantIds()
    );
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/{chatId}/participants")
  public ResponseEntity<Void> addParticipant(
    @PathVariable Long chatId,
    @RequestBody  Long userId,
    Authentication authentication) {
    Long addedByUserId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    chatService.addParticipantToGroup(chatId, userId, addedByUserId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/participants/{userId}")
  public ResponseEntity<Void> removeParticipant(
    @PathVariable Long chatId,
    @PathVariable Long userId,
    Authentication authentication) {
    Long userDoingTheRemovingId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    chatService.removeParticipantFromGroup(chatId, userId, userDoingTheRemovingId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{chatId}/participants")
  public ResponseEntity<Set<UserDTO>> getChatParticipants(
    @PathVariable Long chatId,
    Authentication authentication) {
    Long currentUserId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    if (!chatService.isUserParticipant(chatId, currentUserId)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Set<User> participants = chatService.getChatParticipants(chatId);

    Set<UserDTO> participantDTOs = participants.stream()
      .map(user -> UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .build())
      .collect(Collectors.toSet());

    return ResponseEntity.ok(participantDTOs);
  }

  @PostMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
    @PathVariable Long chatId,
    Authentication authentication) {
    Long leaverId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    chatService.removeParticipantFromGroup(chatId, leaverId,leaverId);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<ChatResponse>> getChatsByReceiver(Authentication authentication) {
    return ResponseEntity.ok(chatService.getChatsByReceiverId(authentication));
  }
}

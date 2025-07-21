package com.example.LiveChattingApp.chat;
import com.example.LiveChattingApp.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chats")
@RestController
@Tag(name="Chat")
public class ChatController {

  private final ChatService chatService;

  @PostMapping("/direct")
  public ResponseEntity<Long> createDirectChat(
    @RequestParam String receiverId,
    Authentication authentication) {
    Long chatId = chatService.createDirectChat(authentication.getName(), receiverId);
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/group")
  public ResponseEntity<Long> createGroupChat(
    @RequestBody CreateGroupChatRequest request,
    Authentication authentication) {
    Long chatId = chatService.createGroupChat(
      authentication.getName(),
      request.getName(),
      request.getParticipantIds()
    );
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/{chatId}/participants")
  public ResponseEntity<Void> addParticipant(
    @PathVariable Long chatId,
    @RequestBody String userId,
    Authentication authentication) {
    chatService.addParticipantToGroup(chatId, userId, authentication.getName());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/participants/{userId}")
  public ResponseEntity<Void> removeParticipant(
    @PathVariable Long chatId,
    @PathVariable String userId,
    Authentication authentication) {
    chatService.removeParticipantFromGroup(chatId, userId, authentication.getName());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{chatId}/participants")
  public ResponseEntity<Set<User>> getChatParticipants(
    @PathVariable Long chatId,
    Authentication authentication) {
    if (!chatService.isUserParticipant(chatId, authentication.getName())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Set<User> participants = chatService.getChatParticipants(chatId);
    return ResponseEntity.ok(participants);
  }

  @PostMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
    @PathVariable Long chatId,
    Authentication authentication) {
    chatService.removeParticipantFromGroup(chatId, authentication.getName(), authentication.getName());
    return ResponseEntity.ok().build();
  }

}

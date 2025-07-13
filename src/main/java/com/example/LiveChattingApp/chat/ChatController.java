package com.example.LiveChattingApp.chat;
import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats")
@RestController
public class ChatController {

  private final ChatService chatService;

  @MessageMapping("/chat.sendMessage")
  @SendTo("/topic/public")
  public Message sendMessage(
    @Payload Message message){
      return message;
  }

  @PostMapping("/direct")
  public ResponseEntity<String> createDirectChat(
    @RequestParam String receiverId,
    Authentication authentication) {
    String chatId = chatService.createDirectChat(authentication.getName(), receiverId);
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/group")
  public ResponseEntity<String> createGroupChat(
    @RequestBody CreateGroupChatRequest request,
    Authentication authentication) {
    String chatId = chatService.createGroupChat(
      authentication.getName(),
      request.getName(),
      request.getParticipantIds()
    );
    return ResponseEntity.ok(chatId);
  }

  @PostMapping("/{chatId}/participants")
  public ResponseEntity<Void> addParticipant(
    @PathVariable String chatId,
    @RequestBody String userId,
    Authentication authentication) {
    chatService.addParticipantToGroup(chatId, userId, authentication.getName());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{chatId}/participants/{userId}")
  public ResponseEntity<Void> removeParticipant(
    @PathVariable String chatId,
    @PathVariable String userId,
    Authentication authentication) {
    chatService.removeParticipantFromGroup(chatId, userId, authentication.getName());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{chatId}/participants")
  public ResponseEntity<Set<User>> getChatParticipants(
    @PathVariable String chatId,
    Authentication authentication) {
    if (!chatService.isUserParticipant(chatId, authentication.getName())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Set<User> participants = chatService.getChatParticipants(chatId);
    return ResponseEntity.ok(participants);
  }

  @PostMapping("/{chatId}/leave")
  public ResponseEntity<Void> leaveChat(
    @PathVariable String chatId,
    Authentication authentication) {
    chatService.removeParticipantFromGroup(chatId, authentication.getName(), authentication.getName());
    return ResponseEntity.ok().build();
  }


}

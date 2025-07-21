package com.example.LiveChattingApp.message;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "Message")
public class MessageController {

  private final MessageService messageService;

  @PostMapping("/direct-chats/{chatId}")
  public ResponseEntity<Void> sendDirectMessage(
    Authentication authentication,
    @PathVariable Long chatId,
    @RequestBody MessageInputDTO messageInput) {
    String senderId = authentication.getName();
    String receiverId = messageService.getMessageReceiverId(senderId, chatId);

    messageService.sendDirectMessage(senderId, receiverId, chatId,messageInput.getContent());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/group-chats/{chatId}")
  public ResponseEntity<Void> sendGroupMessage(
    Authentication authentication,
    @PathVariable Long chatId,
    @RequestBody MessageInputDTO messageInput) {
    String senderId = authentication.getName();

    messageService.sendGroupMessage(senderId, chatId, messageInput.getContent());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}")
  public ResponseEntity<List<MessageResponse>> getChatMessages(
    @PathVariable Long chatId,
    Authentication authentication) {
    List<MessageResponse> messages = messageService.findChatMessages(chatId, authentication);
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/chat/{chatId}/{userId}/read")
  public ResponseEntity<Void> markMessagesAsRead(
    @PathVariable Long chatId,
    String userId){
    messageService.markAsRead(chatId, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/chat/{chatId}/media")
  public ResponseEntity<Void> uploadMediaMessage(
    @PathVariable Long chatId,
    @Parameter()
    @RequestParam("file") MultipartFile file,
    Authentication authentication) {
    messageService.uploadMediaMessage(chatId, file, authentication);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}/is-read")
  public ResponseEntity<Boolean> isChatRead(
    @PathVariable Long chatId,
    Authentication authentication) {
    boolean isRead = messageService.isChatRead(chatId, authentication.getName());
    return ResponseEntity.ok(isRead);
  }

  @GetMapping("/chat/{chatId}/unread-count")
  public ResponseEntity<Long> getUnreadMessageCount(
    @PathVariable Long chatId,
    Authentication authentication) {
    long unreadCount = messageService.getUnreadCount(chatId, authentication.getName());
    return ResponseEntity.ok(unreadCount);
  }


}

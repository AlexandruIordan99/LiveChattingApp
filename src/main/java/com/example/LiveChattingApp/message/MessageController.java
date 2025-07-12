package com.example.LiveChattingApp.message;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  public ResponseEntity<Void> sendMessage(
    @RequestBody MessageRequest request,
    Authentication authentication) {
    messageService.saveMessage(request, authentication);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}")
  public ResponseEntity<List<MessageResponse>> getChatMessages(
    @PathVariable String chatId,
    Authentication authentication) {
    List<MessageResponse> messages = messageService.findChatMessages(chatId, authentication);
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/chat/{chatId}/{userId}/read")
  public ResponseEntity<Void> markMessagesAsRead(
    @PathVariable String chatId,
    String userId){
    messageService.markAsRead(chatId, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/chat/{chatId}/media")
  public ResponseEntity<Void> uploadMediaMessage(
    @PathVariable String chatId,
    @RequestParam("file") MultipartFile file,
    Authentication authentication) {
    messageService.uploadMediaMessage(chatId, file, authentication);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}/is-read")
  public ResponseEntity<Boolean> isChatRead(
    @PathVariable String chatId,
    Authentication authentication) {
    boolean isRead = messageService.isChatRead(chatId, authentication.getName());
    return ResponseEntity.ok(isRead);
  }

  @GetMapping("/chat/{chatId}/unread-count")
  public ResponseEntity<Long> getUnreadMessageCount(
    @PathVariable String chatId,
    Authentication authentication) {
    long unreadCount = messageService.getUnreadCount(chatId, authentication.getName());
    return ResponseEntity.ok(unreadCount);
  }


}

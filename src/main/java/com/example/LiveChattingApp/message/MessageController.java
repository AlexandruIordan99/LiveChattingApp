package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.MessageReadStatus.MessageReadStatus;
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

  @PostMapping("/chat/{chatId}/read")
  public ResponseEntity<Void> markMessagesAsRead(
    @PathVariable String chatId,
    Authentication authentication) {
    messageService.setMessagesToRead(chatId, authentication);
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

  @GetMapping("/{messageId}/read-status")
  public ResponseEntity<List<MessageReadStatus>> getMessageReadStatus(
    @PathVariable String messageId,
    Authentication authentication) {
    List<MessageReadStatus> readStatuses = messageService.getMessageReadStatuses(messageId);
    return ResponseEntity.ok(readStatuses);
  }

  @GetMapping("/chat/{chatId}/unread-count")
  public ResponseEntity<Long> getUnreadMessageCount(
    @PathVariable String chatId,
    Authentication authentication) {
    long unreadCount = messageService.getUnreadMessageCount(chatId, authentication.getName());
    return ResponseEntity.ok(unreadCount);
  }


}

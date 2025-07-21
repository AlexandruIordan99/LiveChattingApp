package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
  private final UserRepository userRepository;

  @PostMapping("/direct-chats/{chatId}")
  public ResponseEntity<Void> sendDirectMessage(
    Authentication authentication,
    @PathVariable Long chatId,
    @RequestBody MessageInputDTO messageInput) {
    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    String receiverId = messageService.getMessageReceiverId(senderId, chatId);

    messageService.sendDirectMessage(senderId, receiverId, chatId, messageInput.getContent());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/group-chats/{chatId}")
  public ResponseEntity<Void> sendGroupMessage(
    Authentication authentication,
    @PathVariable Long chatId,
    @RequestBody MessageInputDTO messageInput) {
    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    messageService.sendGroupMessage(senderId, chatId, messageInput.getContent());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}")
  public ResponseEntity<List<MessageResponse>> getChatMessages(
    @PathVariable Long chatId,
    Authentication authentication) {
    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    List<MessageResponse> messages = messageService.findChatMessages(chatId, senderId);
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/chat/{chatId}/read")
  public ResponseEntity<Void> markMessagesAsRead(
    @PathVariable Long chatId,
    Authentication authentication){

    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    messageService.markAsRead(chatId, senderId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/chat/{chatId}/media")
  public ResponseEntity<Void> uploadMediaMessage(
    @PathVariable Long chatId,
    @RequestParam("file") MultipartFile file,
    Authentication authentication) {

    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    messageService.uploadMediaMessage(chatId, file, senderId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/chat/{chatId}/is-read")
  public ResponseEntity<Boolean> isChatRead(
    @PathVariable Long chatId,
    Authentication authentication) {

    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));

    boolean isRead = messageService.isChatRead(chatId, senderId);
    return ResponseEntity.ok(isRead);
  }

  @GetMapping("/chat/{chatId}/unread-count")
  public ResponseEntity<Integer> getUnreadMessageCount(
    @PathVariable Long chatId,
    Authentication authentication) {
    String senderId = userRepository.findByEmail(authentication.getName()).map(User::getId)
      .orElseThrow(() -> new EntityNotFoundException("Could not find user with this email address."));
    Integer unreadCount = messageService.getUnreadCount(chatId, senderId);
    return ResponseEntity.ok(unreadCount);
  }


}

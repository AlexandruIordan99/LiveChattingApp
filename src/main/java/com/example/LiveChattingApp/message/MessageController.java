package com.example.LiveChattingApp.message;

import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatService;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.messageRequest.MessageRequest;
import com.example.LiveChattingApp.messageRequest.MessageRequestService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Message")
public class MessageController {

  private final MessageService messageService;
  private final FriendshipService friendshipService;
  private final MessageRequestService messageRequestService;
  private final ChatService chatService;

  @PostMapping("/chat/{chatId}")
  public ResponseEntity<Void> sendMessage(
    @RequestBody MessageRequest request,
    Authentication authentication,
    @PathVariable Long chatId) {
    String senderId = authentication.getName();
    String receiverId = request.getReceiverId();

    boolean areFriends = friendshipService.existsFriendshipBetweenUsers(senderId, receiverId);

    if (!areFriends) {
      messageRequestService.getOrCreateMessageRequest(request, senderId, receiverId);
      return ResponseEntity.ok().build();
    }

    Chat chat = chatService.findChatById(chatId);

    messageService.sendMessage(request, authentication, chat);
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

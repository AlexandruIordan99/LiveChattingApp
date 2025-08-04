package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.message.MessageRepository;
import com.example.LiveChattingApp.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatMapper {

  private final MessageRepository messageRepository;

  public ChatResponse toChatResponse(Chat chat, Long currentUserId){

    ChatResponse response = ChatResponse.builder()
      .id(chat.getId())
      .name(chat.getChatName(currentUserId))
      .unreadCount(chat.getUnreadMessagesCount(currentUserId))
      .lastMessage(chat.getLastMessage())
      .lastMessageTime(chat.getLastMessageTime())
      .senderId(currentUserId)
      .build();

    long unreadCount = messageRepository.countUnreadMessagesByChatIdAndUserId(
      chat.getId(), currentUserId);
    response.setUnreadCount(unreadCount);

    messageRepository.findMessagesByChatId(chat.getId())
      .stream()
      .reduce((first, second) -> second)
      .ifPresent(lastMessage -> {
        response.setLastMessage(lastMessage.getContent());
        response.setLastMessageTime(lastMessage.getCreatedDate());
      });

    if (chat.getType() == ChatType.DIRECT) {
      User otherParticipant = chat.getOtherParticipant(currentUserId);
      if (otherParticipant != null) {
        response.setReceiverId(otherParticipant.getId());
        response.setRecipientOnline(otherParticipant.isUserOnline());
      }
    }

    return response;

  }

}

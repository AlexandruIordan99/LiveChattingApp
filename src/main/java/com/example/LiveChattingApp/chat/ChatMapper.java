package com.example.LiveChattingApp.chat;

import com.example.LiveChattingApp.ChatParticipant.ChatParticipant;
import com.example.LiveChattingApp.ChatParticipant.ChatParticipantRepository;
import com.example.LiveChattingApp.ChatParticipant.ChatParticipantResponse;
import com.example.LiveChattingApp.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMapper {

  private final MessageRepository messageRepository;
  private final ChatParticipantRepository participantRepository;

  public ChatResponse toChatResponse(Chat chat, String currentUserId){

    ChatResponse response = ChatResponse.builder()
      .id(chat.getId())
      .name(chat.getChatName(currentUserId))
      .unreadCount(chat.getUnreadMessagesCount(currentUserId))
      .lastMessage(chat.getLastMessage())
      .lastMessageTime(chat.getLastMessageTime())
      .senderId(currentUserId)
      .build();

    List<ChatParticipant> participants = participantRepository
      .findActiveParticipantsByChatId(chat.getId());
    List<ChatParticipantResponse> participantResponses = participants.stream()
      .map(this::toChatParticipantResponse)
      .collect(Collectors.toList());
    response.setParticipants(participantResponses);

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

    return response;

  }

  private ChatParticipantResponse toChatParticipantResponse(ChatParticipant participant) {
    ChatParticipantResponse response = new ChatParticipantResponse();
    response.setId(participant.getId());
    response.setUserId(participant.getUser().getId());
    response.setUserName(participant.getUser().getUsername());
    response.setDisplayName(participant.getUser().getDisplayName());
    response.setRole(participant.getRole());
    response.setJoinedAt(participant.getJoinedAt());
    response.setOnline(participant.getUser().isUserOnline());

    if (participant.getAddedBy() != null) {
      response.setAddedByName(participant.getAddedBy().getDisplayName());
    }

    return response;
  }

}

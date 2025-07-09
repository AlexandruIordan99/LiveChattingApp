package com.example.LiveChattingApp.message;


import com.example.LiveChattingApp.chat.Chat;
import com.example.LiveChattingApp.chat.ChatRepository;
import com.example.LiveChattingApp.user.User;
import com.example.LiveChattingApp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;

  public void saveMessage(MessageRequest messageRequest){
    Chat chat = chatRepository.findById(messageRequest.getChatId())
      .orElseThrow(() -> new RuntimeException("Chat not found"));

    User sender = userRepository.findById(messageRequest.getSenderId())
      .orElseThrow(() -> new RuntimeException("Sender not found"));

    User receiver = userRepository.findById(messageRequest.getReceiverId())
      .orElseThrow(() -> new RuntimeException("Receiver not found"));

    Message message = Message.builder()
      .content(messageRequest.getContent())
      .chat(chat)
      .sender(sender)
      .receiver(receiver)
      .type(messageRequest.getType())
      .state(MessageState.SENT)
      .build();

    messageRepository.save(message);

    //to do notifs!

  }



}

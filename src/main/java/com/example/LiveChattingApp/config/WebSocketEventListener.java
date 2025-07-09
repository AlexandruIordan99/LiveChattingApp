package com.example.LiveChattingApp.config;


import com.example.LiveChattingApp.message.Message;
import com.example.LiveChattingApp.message.MessageType;
import com.example.LiveChattingApp.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

  private final SimpMessageSendingOperations messageTemplate;

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){

    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
    User messageOwner = new User();
    String displayName = messageOwner.getDisplayName();

    if(displayName !=null){
      log.info("{} disconnected.", displayName);
      Message chatMessage = Message.builder()
      .type(MessageType.LEAVE)
        .build();

      messageTemplate.convertAndSend("/topic/public", chatMessage);
    }
  }


}

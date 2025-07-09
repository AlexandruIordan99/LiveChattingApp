package com.example.LiveChattingApp.chat;
import com.example.LiveChattingApp.message.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

  @MessageMapping("/chat.sendMessage")
  @SendTo("/topic/public")
  public Message sendMessage(
    @Payload Message message){
      return message;
  }

}

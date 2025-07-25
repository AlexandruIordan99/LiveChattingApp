package com.example.LiveChattingApp.notification;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final SimpMessagingTemplate messagingTemplate;

  public void sendNotification(String userId, Notification notification) {
    try {
      log.info("Sending WS notification to {} with payload {}", userId, notification);
      messagingTemplate.convertAndSendToUser(
        "topic/notification" + userId,
        "/chat", notification);
    } catch (Exception e) {
      log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
    }

  }
}

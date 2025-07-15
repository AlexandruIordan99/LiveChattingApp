package com.example.LiveChattingApp.messageRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRequestRepository extends JpaRepository<MessageRequest, String> {

  Optional<MessageRequest> findById(MessageRequest messageRequest);


  Optional<MessageRequest> findBySenderIdAndReceiverIdAndMessageRequestStatus(
    @Param("senderId") String senderId,
    @Param("receiverId") String receiverId,
    @Param("messageRequestStatus") MessageRequestStatus messageRequestStatus);

}

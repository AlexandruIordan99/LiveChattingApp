package com.example.LiveChattingApp.messageRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRequestRepository extends JpaRepository<MessageRequest, Long> {

  Optional<MessageRequest> findById(Long messageRequestId);

  @Query("""
      SELECT mr FROM MessageRequest mr
        WHERE ((mr.senderId = :user1 AND mr.receiverId = :user2)
           OR (mr.senderId = :user2 AND mr.receiverId = :user1))
          AND mr.status = :status
""")
  Optional<MessageRequest> findBySenderIdAndReceiverIdAndMessageRequestStatus(
    @Param("senderId") String senderId,
    @Param("receiverId") String receiverId,
    @Param("messageRequestStatus") MessageRequestStatus status);

}

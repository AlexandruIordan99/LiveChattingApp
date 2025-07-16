package com.example.LiveChattingApp.messageRequest;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRequestRepository extends JpaRepository<MessageRequest, Long> {

  @NotNull
  Optional<MessageRequest> findById(@NotNull Long messageRequestId);

  @Query("""
      SELECT mr FROM MessageRequest mr
        WHERE ((mr.senderId = :senderId AND mr.receiverId = :receiverId)
           OR (mr.senderId = :receiverId AND mr.receiverId = :senderId))
          AND mr.status = :status
""")
  Optional<MessageRequest> findBySenderIdAndReceiverIdAndMessageRequestStatus(
    @Param("senderId") String senderId,
    @Param("receiverId") String receiverId,
    @Param("status") MessageRequestStatus status);

}

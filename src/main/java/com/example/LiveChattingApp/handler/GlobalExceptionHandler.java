package com.example.LiveChattingApp.handler;

import com.example.LiveChattingApp.friendship.exceptions.FriendshipAlreadyExistsException;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(FriendshipAlreadyExistsException.class)
  public ResponseEntity<String> handleFriendshipAlreadyExists(FriendshipAlreadyExistsException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(FriendshipNotFoundException.class)
  public ResponseEntity<String> handleFriendshipNotFound(FriendshipNotFoundException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }


}

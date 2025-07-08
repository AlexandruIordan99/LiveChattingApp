package com.example.LiveChattingApp.handler;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Getter
public enum BusinessErrorCodes {

  NO_CODE(0, NOT_IMPLEMENTED,"No code."),
  INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect."),
  NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "New password does not match."),
  ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked."),
  ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled."),
  BAD_CREDENTIALS(304, FORBIDDEN, "Username and/or password is incorrect."),
  USER_DOES_NOT_EXIST(404, NOT_FOUND, "User you tried sending a request to does not exist.")
  ;

  private final int code;
  private final String description;
  private final HttpStatus httpStatus;


  BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
    this.code = code;
    this.httpStatus = httpStatus;
    this.description = description;
  }


}

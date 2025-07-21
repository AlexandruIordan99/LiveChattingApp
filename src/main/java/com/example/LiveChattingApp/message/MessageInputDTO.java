package com.example.LiveChattingApp.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
public class MessageInputDTO {
  @NotBlank
  private String content;

}

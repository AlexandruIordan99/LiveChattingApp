package com.example.LiveChattingApp.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MessageInputDTO {
  @NotBlank
  private String content;

}

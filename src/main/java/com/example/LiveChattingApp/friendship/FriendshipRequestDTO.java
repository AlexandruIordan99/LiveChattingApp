package com.example.LiveChattingApp.friendship;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipRequestDTO {

  @NotNull(message = "Friend ID cannot be null")
  @Min(value = 1, message = "Friend ID must be greater than 0")
  private Long friendId;

}

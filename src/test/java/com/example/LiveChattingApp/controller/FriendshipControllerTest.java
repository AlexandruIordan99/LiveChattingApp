package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipRequestDTO;
import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
import com.example.LiveChattingApp.friendship.FriendshipController;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.security.JwtService;
import com.example.LiveChattingApp.security.UserDetailsServiceImpl;
import com.example.LiveChattingApp.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@WebMvcTest(FriendshipController.class)
public class FriendshipControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FriendshipService friendshipService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserDetailsServiceImpl userDetailsService;

  private User user;
  private FriendshipRequestDTO friendshipRequestDTO;
  private FriendshipResponseDTO friendshipResponseDTO;

  @BeforeEach
  void setUp(){
    user = User.builder()
      .id(1)
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();
      friendshipRequestDTO = new FriendshipRequestDTO();
      friendshipRequestDTO.setFriendId(2);

      friendshipResponseDTO = FriendshipResponseDTO.builder()
        .id(1)
        .userId(2)
        .displayName("gtgmycatisonfire")
        .email("vladloghin259@gmail.com")
        .status("PENDING")
        .createdAt(LocalDateTime.now().toString())
        .isRequester(true)
        .build();
  }

  private static class MockAuthentication implements org.springframework.security.core.Authentication {
    private final User user;

    public MockAuthentication(User user) {
      this.user = user;
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
      return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
      return null;
    }

    @Override
    public Object getDetails() {
      return null;
    }

    @Override
    public Object getPrincipal() {
      return user;
    }

    @Override
    public boolean isAuthenticated() {
      return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Override
    public String getName() {
      return user.getUsername();
    }
  }


  @Test
  @WithMockUser(username = "Jordan299")
  void sendFriendRequest_Success_WithMockUser() throws Exception {
    when(friendshipService.sendFriendRequest(1, 2)).thenReturn(friendshipResponseDTO);

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(user)))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(friendshipRequestDTO)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.userId").value(2))
      .andExpect(jsonPath("$.displayName").value("gtgmycatisonfire"))
      .andExpect(jsonPath("$.email").value("vladloghin259@gmail.com"))
      .andExpect(jsonPath("$.status").value("PENDING"))
      .andExpect(jsonPath("$.requester").value(true));

    verify(friendshipService).sendFriendRequest(1, 2);
  }




}

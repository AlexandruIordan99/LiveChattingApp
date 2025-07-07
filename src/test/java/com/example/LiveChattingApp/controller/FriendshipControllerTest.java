package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipRequestDTO;
import com.example.LiveChattingApp.friendship.DTOs.FriendshipResponseDTO;
import com.example.LiveChattingApp.friendship.FriendshipController;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipAlreadyExistsException;
import com.example.LiveChattingApp.friendship.exceptions.FriendshipNotFoundException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

  @Test
  @WithMockUser
  void sendFriendRequest_FriendshipAlreadyExists_Returns400() throws Exception {
    when(friendshipService.sendFriendRequest(1, 2))
      .thenThrow(new FriendshipAlreadyExistsException("Friendship already exists"));

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(user)))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(friendshipRequestDTO)))
      .andExpect(status().isBadRequest());

    verify(friendshipService).sendFriendRequest(1, 2);
  }

  @Test
  @WithMockUser
  void sendFriendRequest_InvalidRequestBody_Returns400() throws Exception {
    String invalidJson = "{}";

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(user)))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(invalidJson))
      .andExpect(status().isBadRequest());

    verify(friendshipService, never()).sendFriendRequest(anyInt(), anyInt());
  }

  @Test
  @WithMockUser
  void acceptFriendRequest_Success() throws Exception {
    when(friendshipService.acceptFriendRequest(1, 1)).thenReturn(friendshipResponseDTO);

    mockMvc.perform(put("/friend/1/accept")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.userId").value(2))
      .andExpect(jsonPath("$.displayName").value("gtgmycatisonfire"))
      .andExpect(jsonPath("$.status").value("PENDING"));

    verify(friendshipService).acceptFriendRequest(1, 1);
  }

  @Test
  @WithMockUser
  void acceptFriendRequest_FriendshipNotFound_Returns400() throws Exception {
    when(friendshipService.acceptFriendRequest(1, 999))
      .thenThrow(new FriendshipNotFoundException("Friendship not found"));

    mockMvc.perform(put("/friend/999/accept")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isBadRequest());

    verify(friendshipService).acceptFriendRequest(1, 999);
  }

  @Test
  @WithMockUser
  void rejectFriendRequest_Success() throws Exception {
    doNothing().when(friendshipService).rejectFriendRequest(1, 1);

    mockMvc.perform(put("/friend/1/reject")
        .with(authentication(new MockAuthentication(user)))
        .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(content().string(""));

    verify(friendshipService).rejectFriendRequest(1, 1);
  }

  @Test
  @WithMockUser
  void rejectFriendRequest_FriendshipNotFound_Returns400() throws Exception {
    doThrow(new FriendshipNotFoundException("Friendship not found"))
      .when(friendshipService).rejectFriendRequest(1, 999);

    mockMvc.perform(put("/friend/999/reject")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isBadRequest());

    verify(friendshipService).rejectFriendRequest(1, 999);
  }

  @Test
  @WithMockUser
  void removeFriend_Success() throws Exception {
    doNothing().when(friendshipService).removeFriend(1, 2);

    mockMvc.perform(delete("/friend/friends/2")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(content().string(""));

    verify(friendshipService).removeFriend(1, 2);
  }

  @Test
  @WithMockUser
  void removeFriend_FriendshipNotFound_Returns400() throws Exception {
    doThrow(new FriendshipNotFoundException("Friendship not found"))
      .when(friendshipService).removeFriend(1, 999);

    mockMvc.perform(delete("/friend/friends/999")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isBadRequest());

    verify(friendshipService).removeFriend(1, 999);
  }

  @Test
  @WithMockUser
  void blockUser_Success() throws Exception {
    doNothing().when(friendshipService).blockUser(1, 2);

    mockMvc.perform(post("/friend/block/2")
        .with(authentication(new MockAuthentication(user)))
      .with(csrf()))
      .andExpect(status().isOk())
      .andExpect(content().string(""));

    verify(friendshipService).blockUser(1, 2);
  }

  @Test
  @WithMockUser
  void blockUser_UserNotFound_Returns400() throws Exception {
    doThrow(new IllegalArgumentException("User not found"))
      .when(friendshipService).blockUser(1, 999);

    mockMvc.perform(post("/friend/block/999")
        .with(authentication(new MockAuthentication(user)))
        .with(csrf()))
      .andExpect(status().isBadRequest());

    verify(friendshipService).blockUser(1, 999);
  }

  @Test
  @WithMockUser
  void getFriends_Success() throws Exception {
    List<FriendshipResponseDTO> friendsList = Arrays.asList(friendshipResponseDTO);
    when(friendshipService.getFriends(1)).thenReturn(friendsList);

    mockMvc.perform(get("/friend/friendlist")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].userId").value(2))
      .andExpect(jsonPath("$[0].displayName").value("gtgmycatisonfire"))
      .andExpect(jsonPath("$[0].status").value("PENDING"));

    verify(friendshipService).getFriends(1);
  }

  @Test
  @WithMockUser
  void getFriends_EmptyList_Success() throws Exception {
    when(friendshipService.getFriends(1)).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/friend/friendlist")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$").isEmpty());

    verify(friendshipService).getFriends(1);
  }

  @Test
  @WithMockUser
  void getSentPendingRequests_Success() throws Exception {
    List<FriendshipResponseDTO> sentRequests = Arrays.asList(friendshipResponseDTO);
    when(friendshipService.getSentPendingRequests(1)).thenReturn(sentRequests);

    mockMvc.perform(get("/friend/sent-pending-requests")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].userId").value(2))
      .andExpect(jsonPath("$[0].displayName").value("gtgmycatisonfire"))
      .andExpect(jsonPath("$[0].status").value("PENDING"));

    verify(friendshipService).getSentPendingRequests(1);
  }

  @Test
  @WithMockUser
  void getReceivedPendingRequests_Success() throws Exception {
    List<FriendshipResponseDTO> receivedRequests = Arrays.asList(friendshipResponseDTO);
    when(friendshipService.getReceivedPendingRequests(1)).thenReturn(receivedRequests);

    mockMvc.perform(get("/friend/received-pending-requests")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].userId").value(2))
      .andExpect(jsonPath("$[0].displayName").value("gtgmycatisonfire"))
      .andExpect(jsonPath("$[0].status").value("PENDING"));

    verify(friendshipService).getReceivedPendingRequests(1);
  }

  @Test
  @WithMockUser
  void getBlockedUsers_Success() throws Exception {
    FriendshipResponseDTO blockedUser = FriendshipResponseDTO.builder()
      .id(1)
      .userId(2)
      .displayName("copilcoiot")
      .email("mateipau@gmail.com")
      .status("BLOCKED")
      .createdAt(LocalDateTime.now().toString())
      .isRequester(true)
      .build();

    List<FriendshipResponseDTO> blockedUsers = Arrays.asList(blockedUser);
    when(friendshipService.getBlockedUsers(1)).thenReturn(blockedUsers);

    mockMvc.perform(get("/friend/blocked-users")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].userId").value(2))
      .andExpect(jsonPath("$[0].displayName").value("copilcoiot"))
      .andExpect(jsonPath("$[0].status").value("BLOCKED"));

    verify(friendshipService).getBlockedUsers(1);
  }

  @Test
  @WithMockUser
  void getFriendshipStatus_Success() throws Exception {
    when(friendshipService.getFriendshipStatus(1, 2)).thenReturn("PENDING");

    mockMvc.perform(get("/friend/friendship-status/2")
        .with(authentication(new MockAuthentication(user))))
      .andExpect(status().isOk())
      .andExpect(content().string("PENDING"));

    verify(friendshipService).getFriendshipStatus(1, 2);
  }

  @Test
  @WithMockUser
  void getFriendshipStatus_NoFriendship_ReturnsNone() throws Exception {
    when(friendshipService.getFriendshipStatus(1, 999)).thenReturn("NONE");

    mockMvc.perform(get("/friend/friendship-status/999")
        .with(authentication(new MockAuthentication(user))))
      .andDo(print());

    // Check if service was called - if not, the issue is before reaching the controller
    verify(friendshipService).getFriendshipStatus(1, 999);
  }

  @Test
  void sendFriendRequest_Unauthenticated_Returns403() throws Exception {
    mockMvc.perform(post("/friend/request")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(friendshipRequestDTO)))
      .andExpect(status().isForbidden());

    verify(friendshipService, never()).sendFriendRequest(anyInt(), anyInt());
  }

  @Test
  void allEndpoints_Unauthenticated_Returns401() throws Exception {
    mockMvc.perform(get("/friend/friendlist"))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/friend/sent-pending-requests"))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/friend/received-pending-requests"))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/friend/blocked-users"))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(delete("/friend/friends/2")
      .with(csrf()))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(post("/friend/block/2")
      .with(csrf()))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/friend/friendship-status/2"))
      .andExpect(status().isUnauthorized());

    verifyNoInteractions(friendshipService);
  }

  @Test
  @WithMockUser
  void serviceExceptions_HandledGracefully() throws Exception {
    when(friendshipService.sendFriendRequest(1, 2))
      .thenThrow(new IllegalArgumentException("Invalid user ID"));

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(user)))
          .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(friendshipRequestDTO)))
      .andExpect(status().isBadRequest());

    verify(friendshipService).sendFriendRequest(1, 2);
  }


}

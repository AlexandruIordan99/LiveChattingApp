package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.friendship.DTOs.FriendshipRequestDTO;
import com.example.LiveChattingApp.friendship.FriendshipService;
import com.example.LiveChattingApp.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@AutoConfigureWebMvc
@SpringBootTest
public class FriendshipControllerIntegrationTest {


  @Autowired
  private WebApplicationContext context;

  @Autowired
  private FriendshipService friendshipService;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private User mockUser;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(context)
      .apply(springSecurity())
      .build();

    objectMapper = new ObjectMapper();

    mockUser = User.builder()
      .id(String.valueOf(1))
      .displayName("Jordan299")
      .email("alexandru.iordan99@gmail.com")
      .build();
  }

  @Test
  @WithMockUser
  void handleCorsPreflightRequest() throws Exception {
    mockMvc.perform(options("/friend/request")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "Content-Type"))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void validateRequestBody_SendFriendRequest() throws Exception {
    FriendshipRequestDTO invalidRequest = new FriendshipRequestDTO();

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(mockUser)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleMalformedJson() throws Exception {
    String malformedJson = "{ invalid json }";

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(mockUser)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(malformedJson))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleMissingContentType() throws Exception {
    FriendshipRequestDTO request = new FriendshipRequestDTO();
    request.setFriendId(String.valueOf(2));

    mockMvc.perform(post("/friend/request")
        .with(authentication(new MockAuthentication(mockUser)))
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @WithMockUser
  void handleInvalidPathVariables() throws Exception {
    mockMvc.perform(put("/friend/abc/accept")
        .with(authentication(new MockAuthentication(mockUser))))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleEmptyPathVariables() throws Exception {
    mockMvc.perform(put("/friend//accept")
        .with(authentication(new MockAuthentication(mockUser))))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void handleMethodNotAllowed() throws Exception {
    mockMvc.perform(get("/friend/request")
        .with(authentication(new MockAuthentication(mockUser))))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockUser
  void handleInvalidHttpMethods() throws Exception {
    mockMvc.perform(patch("/friend/friendlist")
        .with(authentication(new MockAuthentication(mockUser))))
      .andExpect(status().isMethodNotAllowed());
  }

  private static class MockAuthentication implements org.springframework.security.core.Authentication {
    private final User user;

    public MockAuthentication(User user) {
      this.user = user;
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
      return java.util.Collections.emptyList();
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

}

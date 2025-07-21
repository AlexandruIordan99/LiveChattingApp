package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.friendship.FriendshipRequestDTO;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


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
    mockMvc.perform(options("/friendship/request")
        .header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST")
        .header("Access-Control-Request-Headers", "Content-Type"))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void validateRequestBody_SendFriendRequest() throws Exception {
    FriendshipRequestDTO invalidRequest = new FriendshipRequestDTO();

    mockMvc.perform(post("/friendship/request")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleMalformedJson() throws Exception {
    String malformedJson = "{ invalid json }";

    mockMvc.perform(post("/friendship/request")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser)))
        .contentType(MediaType.APPLICATION_JSON)
        .content(malformedJson))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleMissingContentType() throws Exception {
    FriendshipRequestDTO request = new FriendshipRequestDTO();
    request.setFriendId(String.valueOf(2));

    mockMvc.perform(post("/friendship/request")
        .with(authentication
          (new MockAuthenticationUtils.MockAuthentication(mockUser)))
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  @WithMockUser
  void handleInvalidPathVariables() throws Exception {
    mockMvc.perform(put("/friendship/abc/accept")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser))))
      .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void handleEmptyPathVariables() throws Exception {
    mockMvc.perform(put("/friendship/accept")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser))))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void handleMethodNotAllowed() throws Exception {
    mockMvc.perform(get("/friendship/request")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser))))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockUser
  void handleInvalidHttpMethods() throws Exception {
    mockMvc.perform(patch("/friendship/friendslist")
        .with(authentication(new MockAuthenticationUtils.MockAuthentication(mockUser))))
      .andExpect(status().isMethodNotAllowed());
  }



}

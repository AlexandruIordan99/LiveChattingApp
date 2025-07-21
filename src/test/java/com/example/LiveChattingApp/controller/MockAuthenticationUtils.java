package com.example.LiveChattingApp.controller;

import com.example.LiveChattingApp.user.User;

public class MockAuthenticationUtils {

  public static class MockAuthentication implements org.springframework.security.core.Authentication {
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

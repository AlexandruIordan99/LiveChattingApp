package com.example.LiveChattingApp.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByToken(String token); //Optional is a wrapper to avoid null pointer exceptions


}

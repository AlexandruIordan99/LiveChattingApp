package com.example.LiveChattingApp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);  //finds a user by their email
                                                  //optional used to avoid null pointer errors
    @Query("""
    select user from User user\s
    where user.id != :userId
""")
    Optional<User> findAllUsersExceptSelf(@Param("userId") Integer userId);

    @Query("""
    select user from User user \s
    where user.id == :userId
""")
    Optional<User> findUserById(@Param("userId") Integer userId);
}

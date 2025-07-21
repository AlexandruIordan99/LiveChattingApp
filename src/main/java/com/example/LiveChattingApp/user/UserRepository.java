package com.example.LiveChattingApp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);  //finds a user by their email
                                                  //optional used to avoid null pointer errors
    @Query("""
    select user from User user\s
    where user.id != :userId
""")
    List<User> findAllUsersExceptSelf(@Param("userId") String userId);


    @Query("""
     select user from User user
     where user.displayName = :displayName
""")
    Optional<User> findByDisplayName(@Param("displayName") String displayName);


}

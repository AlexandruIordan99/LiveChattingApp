package com.example.LiveChattingApp.role;

import com.example.LiveChattingApp.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role")
@EntityListeners(AuditingEntityListener.class)



public class Role {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles") //many roles to many users/admins etc.
    @JsonIgnore //to ignore serialization for user
    private List<User> users;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate; //we want to keep track of the user creation date and never update it
    @LastModifiedDate
    @Column(insertable = false) //when creating a new record, we do not want to initialize the value of this variable
    private LocalDateTime modifiedDate;

}

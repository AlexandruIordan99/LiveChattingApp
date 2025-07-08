package com.example.LiveChattingApp.user;

import com.example.LiveChattingApp.common.BaseAuditingEntity;
import com.example.LiveChattingApp.friendship.Friendship;
import com.example.LiveChattingApp.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseAuditingEntity implements UserDetails, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstname;
    private String lastname;
    private String displayName;
    private String dateOfBirth;
    @Column(unique = true)
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enabled;


    @ManyToMany(fetch= FetchType.EAGER) //when you fetch the user, do so eagerly
    @JoinTable(
        name="users_roles",
        joinColumns = @JoinColumn(name="roles_id"),
        inverseJoinColumns = @JoinColumn(name="users_id")
    )
    private List<Role> roles;

    public String fullName(){
        return firstname + " " + lastname;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Friendship> sentFriendRequests;

    @OneToMany(mappedBy = "friend", cascade = CascadeType.ALL)
    private List<Friendship> receivedFriendRequests;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { //for roles and permissions
        return this.roles
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return email;
    }
}

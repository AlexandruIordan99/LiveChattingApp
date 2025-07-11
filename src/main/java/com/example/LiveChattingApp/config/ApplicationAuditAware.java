package com.example.LiveChattingApp.config;

import com.example.LiveChattingApp.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

//Need this class to tell spring to fetch auditors
public class ApplicationAuditAware implements AuditorAware<String> {


    @NotNull
    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated() ||
        authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }

        User userPrincipal = (User) authentication.getPrincipal();

        return Optional.ofNullable(userPrincipal.getId());
    }
}

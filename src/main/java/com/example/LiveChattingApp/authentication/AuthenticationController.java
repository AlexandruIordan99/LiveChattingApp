package com.example.LiveChattingApp.authentication;


import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(@RequestBody @Valid RegistrationRequest request)
      throws MessagingException {

        service.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody @Valid AuthenticationRequest request){
        try {
        return ResponseEntity.ok(service.authenticate(request));
        } catch (RuntimeException invalidCredentialsException){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(invalidCredentialsException);
    }
}

    @GetMapping("/activate-account")
    public ResponseEntity<?> confirm(@RequestParam String token)
            throws MessagingException {
        try{
        service.activateAccount(token);
            return ResponseEntity.ok().build();
    } catch (MessagingException | RuntimeException tokenExpiredException){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(tokenExpiredException.getMessage());
        }
        }
}

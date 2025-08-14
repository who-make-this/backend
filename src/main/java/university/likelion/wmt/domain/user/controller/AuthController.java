package university.likelion.wmt.domain.user.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.user.dto.request.SignInRequest;
import university.likelion.wmt.domain.user.dto.request.SignUpRequest;
import university.likelion.wmt.domain.user.dto.response.TokenResponse;
import university.likelion.wmt.domain.user.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        TokenResponse tokenResponse = authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody SignInRequest signInRequest) {
        TokenResponse tokenResponse = authService.signIn(signInRequest);
        return ResponseEntity.ok(tokenResponse);
    }
}

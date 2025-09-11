package university.likelion.wmt.domain.user.service;

import java.util.Objects;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.common.auth.JwtClaims;
import university.likelion.wmt.common.auth.JwtUserDetails;
import university.likelion.wmt.common.auth.JwtUtils;
import university.likelion.wmt.common.auth.Tokens;
import university.likelion.wmt.domain.user.dto.request.SignInRequest;
import university.likelion.wmt.domain.user.dto.request.SignOutRequest;
import university.likelion.wmt.domain.user.dto.request.SignUpRequest;
import university.likelion.wmt.domain.user.dto.request.TokenRefreshRequest;
import university.likelion.wmt.domain.user.dto.response.TokenResponse;
import university.likelion.wmt.domain.user.entity.Role;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.RefreshTokenRepository;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    @Transactional
    public TokenResponse signUp(SignUpRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserException(UserErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .nickname(request.nickname())
            .build();
        userRepository.save(user);

        JwtClaims claims = new JwtClaims(user.getId(), user.getRole());
        Tokens tokens = jwtUtils.generateTokens(claims);
        refreshTokenRepository.save(user.getId(), tokens.refreshToken());

        return new TokenResponse(tokens.accessToken(), tokens.accessTokenExpiresIn(), tokens.refreshToken(), tokens.refreshTokenExpiresIn());
    }

    public TokenResponse signIn(SignInRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            UserDetails userDetails = (UserDetails)authentication.getPrincipal();
            Long userId = (userDetails instanceof JwtUserDetails jwtUserDetails) ? jwtUserDetails.getUserId() : null;
            if (userId == null) {
                throw new UserException(UserErrorCode.USER_INFO_INVALID);
            }

            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseGet(Role.ROLE_USER::getKey);
            Role userRole = Role.valueOf(role);

            JwtClaims claims = new JwtClaims(userId, userRole);
            Tokens tokens = jwtUtils.generateTokens(claims);
            refreshTokenRepository.save(userId, tokens.refreshToken());

            return new TokenResponse(tokens.accessToken(), tokens.accessTokenExpiresIn(), tokens.refreshToken(), tokens.refreshTokenExpiresIn());
        } catch (BadCredentialsException ex) {
            throw new UserException(UserErrorCode.USER_INFO_INVALID);
        }
    }

    public void signOut(SignOutRequest request) {
        String accessToken = request.accessToken();
        String refreshToken = request.refreshToken();

        JwtClaims claims = jwtUtils.getClaims(accessToken)
            .orElseThrow(() -> new UserException(UserErrorCode.TOKEN_INVALID));
        Long userId = refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new UserException(UserErrorCode.TOKEN_EXPIRED));
        if (!Objects.equals(claims.userId(), userId)) {
            throw new UserException(UserErrorCode.TOKEN_INVALID);
        }

        refreshTokenRepository.delete(userId, refreshToken);
    }

    public TokenResponse refresh(TokenRefreshRequest request) {
        String accessToken = request.accessToken();
        String refreshToken = request.refreshToken();

        JwtClaims claims = jwtUtils.getClaims(accessToken)
            .orElseThrow(() -> new UserException(UserErrorCode.TOKEN_INVALID));
        Long userId = refreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new UserException(UserErrorCode.TOKEN_EXPIRED));
        if (!Objects.equals(claims.userId(), userId)) {
            throw new UserException(UserErrorCode.TOKEN_INVALID);
        }

        refreshTokenRepository.delete(userId, refreshToken);

        Tokens tokens = jwtUtils.generateTokens(claims);
        refreshTokenRepository.save(userId, tokens.refreshToken());

        return new TokenResponse(tokens.accessToken(), tokens.accessTokenExpiresIn(), tokens.refreshToken(), tokens.refreshTokenExpiresIn());
    }
}

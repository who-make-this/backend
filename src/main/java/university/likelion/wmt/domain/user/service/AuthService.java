package university.likelion.wmt.domain.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.common.auth.JwtClaims;
import university.likelion.wmt.common.auth.JwtUserDetails;
import university.likelion.wmt.common.auth.JwtUtils;
import university.likelion.wmt.domain.user.dto.request.SignInRequest;
import university.likelion.wmt.domain.user.dto.request.SignUpRequest;
import university.likelion.wmt.domain.user.dto.response.TokenResponse;
import university.likelion.wmt.domain.user.entity.Role;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

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
        String accessToken = jwtUtils.generateJwtToken(claims);

        return new TokenResponse(accessToken);
    }

    public TokenResponse signIn(SignInRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            UserDetails userDetails = (UserDetails)authentication.getPrincipal();
            Long userId = (userDetails instanceof JwtUserDetails jwtUserDetails) ? jwtUserDetails.getUserId() : null;

            String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseGet(Role.ROLE_USER::getKey);
            Role userRole = Role.valueOf(role);

            JwtClaims claims = new JwtClaims(userId, userRole);
            String accessToken = jwtUtils.generateJwtToken(claims);

            return new TokenResponse(accessToken);
        } catch (BadCredentialsException ex) {
            throw new UserException(UserErrorCode.USER_INFO_INVALID);
        }
    }
}

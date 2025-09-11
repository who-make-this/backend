package university.likelion.wmt.common.auth;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(HttpHeaders.AUTHORIZATION) == null
            || Arrays.stream(AuthConfig.ACCEPTED_URL_LIST).anyMatch(s -> s.equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            String unauthenticatedToken = resolveAccessToken(request)
                .orElseThrow(() -> new UserException(UserErrorCode.NEED_AUTHORIZED));

            JwtClaims claims = jwtUtils.parseToken(unauthenticatedToken);

            Authentication authentication = new JwtUserAuthentication(
                Collections.singletonList(new SimpleGrantedAuthority(claims.userRole().getKey())),
                claims.userId());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException ex) {
            throw new UserException(UserErrorCode.TOKEN_EXPIRED);
        } catch (JwtException ex) {
            throw new UserException(UserErrorCode.TOKEN_INVALID);
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader)) {
            return Optional.empty();
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        return Optional.of(authorizationHeader.substring(BEARER_PREFIX.length()));
    }
}

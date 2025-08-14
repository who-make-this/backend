package university.likelion.wmt.common.auth;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.user.exception.UserErrorCode;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ProblemDetail detail = ProblemDetail.forStatus(UserErrorCode.NEED_AUTHORIZED.getHttpStatus());
        if (!Objects.isNull(UserErrorCode.NEED_AUTHORIZED.getDocumentationUri())) {
            detail.setType(URI.create(UserErrorCode.NEED_AUTHORIZED.getDocumentationUri()));
        }
        detail.setDetail(UserErrorCode.NEED_AUTHORIZED.getMessage());
        detail.setProperty("code", UserErrorCode.NEED_AUTHORIZED.getCode());
        detail.setProperty("timestamp", Instant.now());

        objectMapper.writeValue(response.getWriter(), detail);
    }

}

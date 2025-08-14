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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.user.exception.UserErrorCode;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
        AccessDeniedException accessDeniedException) throws IOException,
        ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ProblemDetail detail = ProblemDetail.forStatus(UserErrorCode.ACCESS_DENIED.getHttpStatus());
        if (!Objects.isNull(UserErrorCode.ACCESS_DENIED.getDocumentationUri())) {
            detail.setType(URI.create(UserErrorCode.ACCESS_DENIED.getDocumentationUri()));
        }
        detail.setDetail(UserErrorCode.ACCESS_DENIED.getMessage());
        detail.setProperty("code", UserErrorCode.ACCESS_DENIED.getCode());
        detail.setProperty("timestamp", Instant.now());

        objectMapper.writeValue(response.getWriter(), detail);
    }

}

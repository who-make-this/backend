package university.likelion.wmt.common.exception;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(final BusinessException ex) {
        log.warn("비즈니스 로직 수행 중 오류가 발생했습니다: {}", ex.getCode());

        ProblemDetail detail = ProblemDetail.forStatus(ex.getHttpStatus());
        if (!Objects.isNull(ex.getDocumentationUri())) {
            detail.setType(URI.create(ex.getDocumentationUri()));
        }
        detail.setDetail(ex.getMessage());
        detail.setProperty("code", ex.getCode());
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(CommonErrorCode.INVALID_HTTP_METHOD.getHttpStatus());
        detail.setDetail(CommonErrorCode.INVALID_HTTP_METHOD.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNotFoundException(final NoResourceFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(CommonErrorCode.INVALID_ENDPOINT.getHttpStatus());
        detail.setDetail(CommonErrorCode.INVALID_ENDPOINT.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError)error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail detail = ProblemDetail.forStatus(CommonErrorCode.INVALID_REQUEST_BODY.getHttpStatus());
        detail.setDetail(CommonErrorCode.INVALID_REQUEST_BODY.getMessage());
        detail.setProperty("errors", errors);
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(CommonErrorCode.INVALID_REQUEST_BODY.getHttpStatus());
        detail.setDetail(CommonErrorCode.INVALID_REQUEST_BODY.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(final Exception ex) {
        log.error("알 수 없는 오류가 발생했습니다: ", ex);

        ProblemDetail detail = ProblemDetail.forStatus(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getHttpStatus());
        detail.setDetail(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getMessage());
        detail.setProperty("timestamp", Instant.now());

        return detail;
    }
}

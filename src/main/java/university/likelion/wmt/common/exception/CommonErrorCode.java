package university.likelion.wmt.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum CommonErrorCode implements ErrorCode {
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, 10000001, "예상치 못한 서버 오류가 발생했습니다.", null),
    INVALID_ENDPOINT(NOT_FOUND, 10000002, "잘못된 API 엔드포인트 URI로 요청했습니다.", null),
    INVALID_HTTP_METHOD(METHOD_NOT_ALLOWED, 10000003, "잘못된 HTTP 메서드로 요청했습니다.", null),
    INVALID_REQUEST_BODY(BAD_REQUEST, 10000004, "잘못된 HTTP 요청 바디로 요청했습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    CommonErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

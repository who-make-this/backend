package university.likelion.wmt.domain.user.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum UserErrorCode implements ErrorCode {
    NEED_AUTHORIZED(HttpStatus.UNAUTHORIZED, 48500001, "인증이 필요합니다.", null),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 48500002, "접근 권한이 없습니다.", null),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 48500003, "인증 정보가 만료되었습니다.", null),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 48500004, "인증 정보가 잘못되었습니다.", null),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 48500005, "사용자를 찾을 수 없습니다.", null),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 48500006, "이미 등록되어 있거나 사용할 수 없는 아이디입니다.",
        "https://www.notion.so/users-sign-up-2483f93a94258043ba1bc28a16a55bc7"),
    USER_INFO_INVALID(HttpStatus.UNAUTHORIZED, 48500007, "아이디 또는 비밀번호가 일치하지 않습니다.",
        "https://www.notion.so/users-sign-in-2483f93a9425805c9710f2220db5d5c2");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    UserErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

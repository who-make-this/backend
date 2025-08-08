package university.likelion.wmt.common.exception;

import java.io.Serial;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public String getMessage() {
        return errorCode.getMessage();
    }

    public String getDocumentationUri() {
        return errorCode.getDocumentationUri();
    }
}

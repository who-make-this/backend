package university.likelion.wmt.domain.order.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, 47900001, "주문을 찾을 수 없습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    OrderErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

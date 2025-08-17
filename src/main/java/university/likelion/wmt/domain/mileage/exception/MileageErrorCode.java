package university.likelion.wmt.domain.mileage.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum MileageErrorCode implements ErrorCode {
    USABLE_BALANCE_INSUFFICIENT(HttpStatus.CONFLICT, 47700001, "사용할 수 있는 마일리지가 부족합니다.", null),
    MONTHLY_EARN_CAP_EXCEEDED(HttpStatus.CONFLICT, 47700002, "한 달에 적립 가능한 마일리지를 초과했습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    MileageErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

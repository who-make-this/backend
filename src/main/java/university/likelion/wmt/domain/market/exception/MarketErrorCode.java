package university.likelion.wmt.domain.market.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum MarketErrorCode implements ErrorCode {
    MARKET_NOT_FOUND(HttpStatus.NOT_FOUND, 47720001, "시장을 찾을 수 없습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    MarketErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

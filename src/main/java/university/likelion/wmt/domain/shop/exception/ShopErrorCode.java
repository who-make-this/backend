package university.likelion.wmt.domain.shop.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum ShopErrorCode implements ErrorCode {
    MERCHANDISE_NOT_FOUND(HttpStatus.NOT_FOUND, 41090001, "상품을 찾을 수 없습니다.", null),
    MERCHANDISE_NOT_PURCHASABLE(HttpStatus.CONFLICT, 41090002, "품절 등으로 구입할 수 없는 상품입니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    ShopErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

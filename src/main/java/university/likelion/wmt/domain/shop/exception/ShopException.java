package university.likelion.wmt.domain.shop.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class ShopException extends BusinessException {
    public ShopException(ErrorCode errorCode) {
        super(errorCode);
    }
}

package university.likelion.wmt.domain.order.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class OrderException extends BusinessException {
    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}

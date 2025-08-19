package university.likelion.wmt.domain.market.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class MarketException extends BusinessException {
    public MarketException(ErrorCode errorCode) {
        super(errorCode);
    }
}

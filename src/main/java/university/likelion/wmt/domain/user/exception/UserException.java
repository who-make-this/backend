package university.likelion.wmt.domain.user.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class UserException extends BusinessException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}

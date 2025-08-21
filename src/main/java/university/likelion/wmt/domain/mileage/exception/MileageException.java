package university.likelion.wmt.domain.mileage.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class MileageException extends BusinessException {
    public MileageException(ErrorCode errorCode) {
        super(errorCode);
    }
}

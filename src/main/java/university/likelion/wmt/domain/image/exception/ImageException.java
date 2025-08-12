package university.likelion.wmt.domain.image.exception;

import university.likelion.wmt.common.exception.BusinessException;
import university.likelion.wmt.common.exception.ErrorCode;

public class ImageException extends BusinessException {
    public ImageException(ErrorCode errorCode) {
        super(errorCode);
    }
}

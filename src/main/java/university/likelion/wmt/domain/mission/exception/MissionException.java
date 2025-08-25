package university.likelion.wmt.domain.mission.exception;

import lombok.Getter;

import university.likelion.wmt.common.exception.BusinessException;

@Getter
public class MissionException extends BusinessException {
    public MissionException(MissionErrorCode errorCode){
        super(errorCode);
    }
}

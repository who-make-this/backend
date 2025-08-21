package university.likelion.wmt.domain.mission.exception;

import lombok.Getter;

@Getter
public class MissionException extends RuntimeException {
    private final MissionErrorCode errorCode;
    public MissionException(MissionErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

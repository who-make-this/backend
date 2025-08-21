package university.likelion.wmt.domain.mission.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode {
    //400 Bad Request
    INVALID_MISSION_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 미션 카테고리입니다."),
    MISSION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 진행중인 미션이 있습니다. "),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 미션입니다."),
    MISSION_NOT_FOUND(HttpStatus.BAD_REQUEST, "진행 중인 미션을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "이미지를 찾을 수 없습니다."), //이미지 예외
    ALREADY_STARTED_TODAY(HttpStatus.BAD_REQUEST, "탐험은 하루에 한번만 가능합니다."),

    //500 Internal Server Error
    AI_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "미션 생성에 실패했습니다."),
    DUPLICATE_MISSION_ID(HttpStatus.INTERNAL_SERVER_ERROR, "중복된 미션 ID가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

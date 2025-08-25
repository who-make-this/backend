package university.likelion.wmt.domain.mission.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum MissionErrorCode implements ErrorCode {
    //400 Bad Request
    INVALID_MISSION_TYPE(HttpStatus.BAD_REQUEST, 47730001, "지원하지 않는 미션 카테고리입니다.", null),
    MISSION_ALREADY_STARTED(HttpStatus.BAD_REQUEST, 47730002, "이미 진행중인 미션이 있습니다.", null),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, 47730003, "이미 완료된 미션입니다.", null),
    MISSION_NOT_FOUND(HttpStatus.BAD_REQUEST, 47730004, "진행 중인 미션을 찾을 수 없습니다.", null),
    IMAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, 47730005, "이미지를 찾을 수 없습니다.", null), //이미지 예외
    ALREADY_STARTED_TODAY(HttpStatus.BAD_REQUEST, 47730006, "탐험은 하루에 한번만 가능합니다.", null),

    //500 Internal Server Error
    AI_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 47730007, "미션 생성에 실패했습니다.", null),
    DUPLICATE_MISSION_ID(HttpStatus.INTERNAL_SERVER_ERROR, 47730008, "중복된 미션 ID가 발생했습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    MissionErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

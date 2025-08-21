package university.likelion.wmt.domain.report.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ReportErrorCode {
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 보고서를 찾을 수 없습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 보고서입니다."),

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션의 이미지를 찾을 수 없습니다. ");

    private final HttpStatus httpStatus;
    private final String message;
}

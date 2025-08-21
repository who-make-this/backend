package university.likelion.wmt.domain.image.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

import university.likelion.wmt.common.exception.ErrorCode;

@Getter
public enum ImageErrorCode implements ErrorCode {
    IMAGE_FILE_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 47300001, "파일을 읽던 중 오류가 발생했습니다.", null),
    IMAGE_MIME_NOT_ALLOWED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, 47300002, "업로드할 수 없는 파일 형식입니다.", null),
    CLOUDFLARE_IMAGES_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, 47300003, "파일 서버와 통신에 실패했습니다.", null);

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
    private final String documentationUri;

    ImageErrorCode(HttpStatus httpStatus, int code, String message, String documentationUri) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.documentationUri = documentationUri;
    }
}

package university.likelion.wmt.domain.image.implement;

import java.io.IOException;
import java.util.Objects;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import university.likelion.wmt.domain.image.exception.ImageErrorCode;
import university.likelion.wmt.domain.image.exception.ImageException;
import university.likelion.wmt.domain.image.property.ImageProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageValidator {
    private final ImageProperties properties;
    private static final Tika tika = new Tika();

    public void validateAllowedMime(MultipartFile file) {
        // 파일 확장자 기반이 아닌 실제 파일의 MIME를 파악
        String mimeFromContent = null;
        try {
            mimeFromContent = tika.detect(file.getInputStream());
        } catch (IOException ignored) {
            throw new ImageException(ImageErrorCode.IMAGE_FILE_IO_ERROR);
        }

        // 파일의 MIME가 허용된 MIME인지 확인하여 아니라면 예외를 발생시킴
        String mime = mimeFromContent;
        if (properties.getAllowedMime().stream().noneMatch(allowed -> Objects.equals(allowed, mime))) {
            throw new ImageException(ImageErrorCode.IMAGE_MIME_NOT_ALLOWED);
        }
    }
}

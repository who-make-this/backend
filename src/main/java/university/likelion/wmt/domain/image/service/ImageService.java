package university.likelion.wmt.domain.image.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.dto.response.ImageResponse;
import university.likelion.wmt.domain.image.implement.ImageValidator;
import university.likelion.wmt.domain.image.implement.ImageWriter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final ImageValidator imageValidator;
    private final ImageWriter imageWriter;

    @Transactional
    public ImageResponse upload(MultipartFile file) {
        imageValidator.validateAllowedMime(file);
        String uri = imageWriter.upload(file);

        return new ImageResponse(uri);
    }

    @Transactional
    public void delete(Long imageId) {
        imageWriter.delete(imageId);
    }
}

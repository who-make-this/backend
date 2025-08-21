package university.likelion.wmt.domain.image.implement;

import java.util.Objects;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.image.client.CloudflareImagesClient;
import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.image.exception.ImageErrorCode;
import university.likelion.wmt.domain.image.exception.ImageException;
import university.likelion.wmt.domain.image.property.ImageProperties;
import university.likelion.wmt.domain.image.repository.ImageRepository;

@Component
@RequiredArgsConstructor
public class ImageWriter {
    private static final String IMAGE_BASE_URI = "https://imagedelivery.net";
    private static final String IMAGE_VARIANTS_PUBLIC = "public";

    private final ImageRepository imageRepository;
    private final CloudflareImagesClient client;
    private final ImageProperties properties;

    public String upload(MultipartFile file) {
        String cfName = client.upload(file);
        if (Objects.isNull(cfName)) {
            throw new ImageException(ImageErrorCode.CLOUDFLARE_IMAGES_UPLOAD_FAILED);
        }

        Image image = Image.builder()
            .cfName(cfName)
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .build();
        imageRepository.save(image);

        String uri = String.format("%s/%s/%s/%s", IMAGE_BASE_URI, properties.getAccountHash(), cfName,
            IMAGE_VARIANTS_PUBLIC);

        return uri;
    }

    public void delete(Long imageId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("image"));

        client.delete(image.getCfName());
        imageRepository.delete(image);
    }
    // 새롭게 추가된 메서드: cfName을 받아 이미지 URL을 생성하고 반환합니다.
    public String createImageUrl(String cfName) {
        return String.format("%s/%s/%s/%s",
            IMAGE_BASE_URI,
            properties.getAccountHash(),
            cfName,
            IMAGE_VARIANTS_PUBLIC);
    }
}

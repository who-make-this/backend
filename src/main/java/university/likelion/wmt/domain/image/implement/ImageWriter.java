package university.likelion.wmt.domain.image.implement;

import java.util.Objects;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.image.exception.ImageErrorCode;
import university.likelion.wmt.domain.image.exception.ImageException;
import university.likelion.wmt.domain.image.property.ImageProperties;
import university.likelion.wmt.domain.image.repository.ImageRepository;
import university.likelion.wmt.domain.image.service.CloudflareImagesClient;

@Component
@RequiredArgsConstructor
public class ImageWriter {
    private static String IMAGE_BASE_URI = "https://imagedelivery.net";
    private static String IMAGE_VARIANTS_PUBLIC = "public";

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
}

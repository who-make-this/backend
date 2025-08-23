package university.likelion.wmt.domain.image.implement;

import java.util.Objects;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public String upload(MultipartFile file) {
        String cfName = client.upload(file);
        if (Objects.isNull(cfName)) {
            throw new ImageException(ImageErrorCode.CLOUDFLARE_IMAGES_UPLOAD_FAILED);
        }

        String imageUrl = getImageUri(cfName);

        Image image = Image.builder()
            .cfName(cfName)
            .fileSize(file.getSize())
            .imageUrl(imageUrl)
            .contentType(file.getContentType())
            .build();

        imageRepository.save(image);

        return imageUrl;
    }

    @Transactional
    public void delete(Long imageId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("image"));

        client.delete(image.getCfName());
        imageRepository.delete(image);
    }

    private String getImageUri(String cfName) {
        return String.format("%s/%s/%s/%s",
            IMAGE_BASE_URI,
            properties.getAccountHash(),
            cfName,
            IMAGE_VARIANTS_PUBLIC);
    }
}

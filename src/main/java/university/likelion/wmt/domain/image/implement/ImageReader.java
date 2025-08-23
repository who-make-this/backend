package university.likelion.wmt.domain.image.implement;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.image.property.ImageProperties;
import university.likelion.wmt.domain.image.repository.ImageRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageReader {
    private static final String IMAGE_BASE_URI = "https://imagedelivery.net";
    private static final String IMAGE_VARIANTS_PUBLIC = "public";

    private final ImageRepository imageRepository;
    private final ImageProperties properties;

    public List<String> get(String refType, Long refId) {
        return imageRepository.findAllByRefTypeAndRefId(refType, refId).stream()
            .map(image -> getImageUri(image.getCfName()))
            .toList();
    }

    private String getImageUri(String cfName) {
        return String.format("%s/%s/%s/%s",
            IMAGE_BASE_URI,
            properties.getAccountHash(),
            cfName,
            IMAGE_VARIANTS_PUBLIC);
    }
}

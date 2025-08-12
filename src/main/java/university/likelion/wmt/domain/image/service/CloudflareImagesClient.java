package university.likelion.wmt.domain.image.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import lombok.extern.slf4j.Slf4j;

import university.likelion.wmt.domain.image.property.ImageProperties;

@Slf4j
@Component
public class CloudflareImagesClient {
    private static final String HTTP_AUTHORIZATION_HEADER_SCHEME_BEARER = "Bearer ";
    private static final String CLOUDFLARE_IMAGES_API_BASE_URI = "https://api.cloudflare.com/client/v4";
    private static final String CLOUDFLARE_IMAGES_API_PATH = "/accounts/{accountId}/images/v1";

    private final ImageProperties properties;
    private final WebClient webClient;

    public CloudflareImagesClient(ImageProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
            .baseUrl(CLOUDFLARE_IMAGES_API_BASE_URI)
            .defaultHeader(HttpHeaders.AUTHORIZATION, HTTP_AUTHORIZATION_HEADER_SCHEME_BEARER + properties.getApikey())
            .build();
    }

    public String upload(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
            .filename(Objects.requireNonNull(file.getOriginalFilename()))
            .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));

        try {
            CloudflareImagesUploadResponse response = webClient.post()
                .uri(CLOUDFLARE_IMAGES_API_PATH, properties.getAccountId())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(CloudflareImagesUploadResponse.class)
                .block();

            return response.result().id();
        } catch (WebClientException ex) {
            log.error("업로드를 위해 Cloudflare Images API 서버와 통신 중 오류가 발생했습니다: {}", ex.getMessage());
        }

        return null;
    }

    public void delete(String cfName) {
        try {
            webClient.delete()
                .uri(CLOUDFLARE_IMAGES_API_PATH + "/{imageId}", properties.getAccountId(), cfName)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (WebClientException ex) {
            log.error("삭제를 위해 Cloudflare Images API 서버와 통신 중 오류가 발생했습니다: {}", ex.getMessage());
        }
    }

    private record CloudflareImagesUploadResponse(
        List<Object> errors,
        List<Object> messages,
        CloudflareImagesUploadResult result,
        Boolean success
    ) {
    }

    private record CloudflareImagesUploadResult(
        String id,
        String filename,
        LocalDateTime uploaded
    ) {
    }
}

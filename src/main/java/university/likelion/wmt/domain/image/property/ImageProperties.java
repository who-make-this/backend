package university.likelion.wmt.domain.image.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "wmt.cloudflare.images")
@RequiredArgsConstructor
@Getter
public class ImageProperties {
    private final String email;
    private final String accountId;
    private final String accountHash;
    private final String apikey;
    private final List<String> allowedMime;
    private final DataSize sizeLimit;
}

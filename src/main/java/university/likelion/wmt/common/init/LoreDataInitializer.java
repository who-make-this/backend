package university.likelion.wmt.common.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.lore.repository.LoreRepository;
import university.likelion.wmt.domain.lore.service.LoreService;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.market.repository.MarketRepository;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoreDataInitializer implements CommandLineRunner {
    private final LoreService loreService;
    private final MarketRepository marketRepository;
    private final ObjectMapper objectMapper;
    private final LoreRepository loreRepository;

    private static final Tika tika = new Tika();

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("로어 데이터 초기화를 시작합니다...");

        List<Market> markets = marketRepository.findAll();
        if(markets.isEmpty()) {
            log.warn("시장을 찾을 수 없습니다. 로어 데이터 초기화를 건너뜁니다.");
            return;
        }
        Market market = markets.get(0);

        if(loreRepository.countByMarket(market) > 0){
            log.info("로어 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        File jsonFile = new ClassPathResource("lore_data.json").getFile();
        List<LoreContent> loreContents = objectMapper.readValue(jsonFile, new TypeReference<>() {});

        for(LoreContent content : loreContents){
                MultipartFile imageFile = convertToMultipartFile("lore_images/" + content.imageFileName());
                Image uploadedImage = loreService.uploadImage(imageFile);
                loreService.saveLoreWithImage(market, content.title(),  content.content(), content.requiredMissionCount(), uploadedImage);
                log.info("로어 초기화 성공: {}", content.title());
        }
        log.info("로어 데이터 초기화가 완료되었습니다.");
    }
    private MultipartFile convertToMultipartFile(String path) throws Exception {
        File file = new ClassPathResource(path).getFile();
        byte[] content = Files.readAllBytes(file.toPath());
        String mimeType = tika.detect(file);
        return new MockMultipartFile(file.getName(), file.getName(), mimeType, content);
    }
    private record LoreContent(String title, String content, long requiredMissionCount, String imageFileName){}
}

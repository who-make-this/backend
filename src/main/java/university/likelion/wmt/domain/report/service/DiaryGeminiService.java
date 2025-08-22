package university.likelion.wmt.domain.report.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import university.likelion.wmt.domain.image.implement.ImageWriter;
import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.report.dto.response.ReportResponse;
import university.likelion.wmt.domain.report.util.DiaryPromptBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryGeminiService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ImageWriter imageWriter;

    @Value("${wmt.gemini.api.key}")
    private String apiKey;

    public String getWeatherFromGemini(LocalDate date) {
        try {
            String weatherPrompt = String.format("대한민국 구미시의 %s 날씨는 어때? 날씨 정보만 한 문장으로 간결하게 답변해줘.", date.toString());
            String geminiApiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key="
                    + apiKey;

            String payload = objectMapper.writeValueAsString(
                new Object() {
                    public final Object[] contents = new Object[] {
                        new Object() {
                            public final String role = "user";
                            public final Object[] parts = new Object[] {
                                new Object() {
                                    public final String text = weatherPrompt;
                                }
                            };
                        }
                    };
                }
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(geminiApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini Weather API 호출 실패. Status: {}, Body: {}", response.statusCode(), response.body());
                return "알 수 없음";
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            return rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API를 이용한 날씨 정보 획득 중 오류 발생: {}", e.getMessage());
            return "알 수 없음";
        }
    }

    public String generateJournal(
        ReportResponse report,
        List<Mission> completedMissions,
        String weatherInfo,
        String selectedImageUrl
    ) {
        try {
            String journalPrompt = DiaryPromptBuilder.buildDiaryPrompt(report, completedMissions, weatherInfo);
            HttpRequest imageRequest = HttpRequest.newBuilder()
                .uri(new URI(selectedImageUrl))
                .GET()
                .build();
            HttpResponse<byte[]> imageResponse = httpClient.send(imageRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (imageResponse.statusCode() != 200) {
                log.error("이미지 다운로드 실패. Status: {}", imageResponse.statusCode());
                return "일기 생성에 실패했씁니다. (이미지 오류)";
            }
            byte[] imageBytes = imageResponse.body();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String geminiApiUrl =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent?key="
                    + apiKey;
            String payload = objectMapper.writeValueAsString(
                new Object() {
                    public final Object[] contents = new Object[] {
                        new Object() {
                            public final String role = "user";
                            public final Object[] parts = new Object[] {
                                new Object() {
                                    public final String text = journalPrompt;
                                },
                                new Object() {
                                    public final Object inlineData = new Object() {
                                        public final String mimeType = "image/jpeg";
                                        public final String data = base64Image;
                                    };
                                }
                            };
                        }
                    };
                }
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(geminiApiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Gemini Journal API 호출에 실패. Status: {}, Body: {}", response.statusCode(), response.body());
                return "일기 생성에 실패했습니다.";
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            String journalText = rootNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

            return journalText;
        } catch (Exception e) {
            log.error("Gemini API를 이용한 일기 생성 중 오류 발생: {}", e.getMessage());
            return "일기 생성 중 오류가 발생했습니다.";
        }
    }
}

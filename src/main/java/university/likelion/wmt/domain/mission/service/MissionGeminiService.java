package university.likelion.wmt.domain.mission.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import university.likelion.wmt.domain.mission.util.MissionPromptBuilder;

@Slf4j
@Service
public class MissionGeminiService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${wmt.gemini.api.key}")
    private String apiKey;

    //미션 메서드만 남기기 - 미션 생성 메서드 삭제
    public String authenticateMission(String missionContent, String category, MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes(); //-> 이미지 바이트 배열로 변환
            String base64Image = Base64.getEncoder().encodeToString(imageBytes); //Base64 문자열로 인코딩

            String prompt = MissionPromptBuilder.buildAuthenticationPrompt(missionContent, category);
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
                                    public final String text = prompt;
                                },
                                new Object() {
                                    public final Object inlineData = new Object() {
                                        public final String mimeType = imageFile.getContentType();
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
                log.error("Gemini Vision API 호출에 실패. Status: {}, Body: {}", response.statusCode(), response.body());
                return "ERROR";
            }
            JsonNode rootNode = objectMapper.readTree(response.body());
            String responseText = rootNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();

            return responseText.trim().toUpperCase();

        } catch (Exception e) {
            log.error("Gemini API를 이용한 미션 인증 중 오류 발생: {}", e.getMessage());
            return "ERROR";
        }
    }
}

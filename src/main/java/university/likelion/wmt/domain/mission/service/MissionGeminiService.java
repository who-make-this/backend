package university.likelion.wmt.domain.mission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import university.likelion.wmt.domain.mission.util.MissionPromptBuilder;

@Slf4j
@Service
public class MissionGeminiService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Value("${wmt.gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent";

    /**
     * Cloudflare Images URL을 받아 이미지 다운로드 → Base64 인라인으로 Gemini 호출
     */
    public String authenticateMission(String missionContent, String category, String imageUrl) {
        try {
            // 1) 이미지 다운로드
            ImageBlob blob = downloadImage(imageUrl);
            if (blob.bytes == null || blob.bytes.length == 0) {
                log.error("이미지 다운로드 실패 또는 빈 바이트");
                return "ERROR";
            }
            String mimeType = (blob.mime != null && blob.mime.startsWith("image/"))
                ? blob.mime
                : MediaType.IMAGE_JPEG_VALUE;

            // 2) 프롬프트 구성
            String prompt = MissionPromptBuilder.buildAuthenticationPrompt(missionContent, category);

            // 3) Base64 인코딩 및 페이로드 구성
            String base64Image = Base64.getEncoder().encodeToString(blob.bytes);
            String payload = buildInlinePayload(prompt, mimeType, base64Image);

            // 4) Gemini 호출
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Gemini Vision API 호출 실패. status={}, body={}", response.statusCode(), response.body());
                return "ERROR";
            }

            JsonNode root = objectMapper.readTree(response.body());
            String responseText = root.path("candidates")
                .path(0).path("content")
                .path( "parts").path(0)
                .path("text").asText();

            return responseText == null ? "ERROR" : responseText.trim().toUpperCase();

        } catch (Exception e) {
            log.error("Gemini 미션 인증 처리 중 오류: {}", e.getMessage(), e);
            return "ERROR";
        }
    }

    private String buildInlinePayload(String prompt, String mimeType, String base64) throws Exception {
        var root = objectMapper.createObjectNode();
        var contents = objectMapper.createArrayNode();
        var content = objectMapper.createObjectNode();
        var parts = objectMapper.createArrayNode();

        var textPart = objectMapper.createObjectNode();
        textPart.put("text", prompt);

        var inlineData = objectMapper.createObjectNode();
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64);

        var inlinePart = objectMapper.createObjectNode();
        inlinePart.set("inline_data", inlineData);

        parts.add(textPart);
        parts.add(inlinePart);
        content.set("parts", parts);
        contents.add(content);
        root.set("contents", contents);
        return objectMapper.writeValueAsString(root);
    }

    private ImageBlob downloadImage(String url) throws Exception {
        // GET으로 바로 내려받되, content-type 헤더를 기록
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();

        HttpResponse<InputStream> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("이미지 다운로드 실패 status=" + resp.statusCode());
        }

        String contentType = resp.headers().firstValue("content-type").orElse("");
        try (InputStream in = resp.body(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            return new ImageBlob(out.toByteArray(), contentType);
        }
    }

    private record ImageBlob(byte[] bytes, String mime) {}
}

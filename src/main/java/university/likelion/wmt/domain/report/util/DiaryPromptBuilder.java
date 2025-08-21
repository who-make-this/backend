package university.likelion.wmt.domain.report.util;

import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.report.dto.response.ReportResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiaryPromptBuilder {

    // static 메서드로 선언되어 있어 클래스 이름으로 직접 호출 가능합니다.
    public static String buildDiaryPrompt(
        ReportResponse report,
        List<Mission> completedMissions,
        String weatherInfo
    ) {
        // null 체크를 통해 NullPointerException을 방지합니다.
        if (report == null) {
            return "보고서 데이터가 올바르지 않아 일기를 생성할 수 없습니다.";
        }

        // 미션 목록이 null이거나 비어있을 경우를 대비해 안전하게 처리합니다.
        String missionListText = Optional.ofNullable(completedMissions)
            .orElse(Collections.emptyList())
            .stream()
            .map(mission -> "- " + Optional.ofNullable(mission.getContent()).orElse(""))
            .collect(Collectors.joining("\n"));

        // 카테고리별 성공 횟수 맵이 null일 경우를 대비해 안전하게 처리합니다.
        String categoryCountsText = Optional.ofNullable(report.completedMissionsByCategories())
            .orElse(Collections.emptyMap())
            .entrySet().stream()
            .map(entry -> String.format("%s 카테고리: %d개", entry.getKey(), Optional.ofNullable(entry.getValue()).orElse(0)))
            .collect(Collectors.joining(", "));

        // 전체 프롬프트 구성
        String prompt = String.format(
            "다음 정보를 바탕으로 시장 탐험 일기를 작성해줘. " +
                "일기는 **100자 내외**로 작성해줘. " +
                "다음 형식과 내용에 맞춰 작성해야 해. " +
                "**특히, 사용자가 제공한 이미지와 미션 내용을 분석하여 감성적인 문체(~했다... ~했지만 ~했다)로 일기를 작성해줘.** " +
                "각 항목 뒤에는 반드시 줄바꿈을 포함해줘.\n\n" +
                "--- 입력 데이터 ---\n" +
                "탐험 날짜: %s\n" +
                "날씨: %s\n" +
                "성공한 미션 목록:\n%s\n" +
                "총 성공 미션 수: %d개\n" +
                "카테고리별 성공 횟수: %s\n" +
                "획득한 총 점수: %d점\n" +
                "--- 일기 형식 ---\n" +
                "제목: [제목 (5단어 이하)]\n" +
                "내용:\n" +
                "[날씨 및 기분 언급]\n" +
                "[미션 내용 (이미지 분석 및 미션 내용)]\n" +
                "[성공 수가 많은 속성에 관련된 멘트]\n" +
                "[마무리 멘트 (오늘 하루를 짧게 요약)]",
            report.startTime() != null ? report.startTime().toLocalDate() : "날짜 정보 없음",
            weatherInfo,
            missionListText,
            completedMissions != null ? completedMissions.size() : 0,
            categoryCountsText,
            report.totalScore()
        );

        return prompt;
    }
}

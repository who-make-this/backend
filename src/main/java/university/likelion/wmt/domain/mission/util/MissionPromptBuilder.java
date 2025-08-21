package university.likelion.wmt.domain.mission.util;

import university.likelion.wmt.domain.mission.entity.MissionFailureReason;

import java.util.List;

public class MissionPromptBuilder {
    private static List<MissionFailureReason> failureReasons;
    public static void setFailureReasons(List<MissionFailureReason> reasons) {
        MissionPromptBuilder.failureReasons = reasons;
    }
    public static String buildAuthenticationPrompt(String missionContent, String category){
        StringBuilder failureReasonList = new StringBuilder();
        if(failureReasons != null){
            for (MissionFailureReason reason : failureReasons) {
                failureReasonList.append(String.format("- %s: %s\n", reason.getCode(), reason.getReason()));
            }
            }

        String basePrompt = String.format("제공된 사진이 다음 미션 '%s'를 수행한 사진입니까? " +
            "사진이 미션 내용과 일치하면 'YES'를, 아니면 'NO'를 출력해주세요. " +
            "만약 사진이 미션과 불일치한다면, 다음 5가지 실패 사유 중 가장 적합한 사유 코드를 선택하여 'NO:실패코드' 형식으로 응답하세요.\n\n" +
            "실패 사유 목록:\n%s\n" +
            "예시) NO:SHAKY_PHOTO", missionContent, failureReasonList.toString());

        return switch(category.toLowerCase()) {
            case "감성형" -> basePrompt + " 사진에서 '추억', '오래된', '정겨운' 등의 감성적 키워드가 연상되는지 중점적으로 판단하세요.";
            case "모험형" -> basePrompt + " 사진이 '길', '간판', '건물' 등 시장의 특정 장소를 탐색한 증거를 보여주는지 판단하세요.";
            case "먹보형" -> basePrompt + " 사진에 '음식'이나 '요리', '식당'과 같은 먹는 행위와 관련된 요소가 포함되어 있는지 판단하세요.";
            default -> basePrompt;
        };
    }
}

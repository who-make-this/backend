package university.likelion.wmt.domain.user.dto.response;

import java.time.LocalDate;

import university.likelion.wmt.domain.user.entity.User;

public record UserInfoResponse(
    String nickname,
    LocalDate createdAt,
    String userType,
    long completedMissionCount,
    long explorationCount
) {
    public static UserInfoResponse of(User user, String userType, long completedMissionCount, long explorationCount) {
        return new UserInfoResponse(user.getNickname(),
            user.getCreatedAt().toLocalDate(),
            userType,
            completedMissionCount,
            explorationCount);
    }
}

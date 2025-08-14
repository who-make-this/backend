package university.likelion.wmt.domain.user.dto.response;

import java.time.LocalDate;

import university.likelion.wmt.domain.user.entity.User;

public record UserInfoResponse(
    String nickname,
    LocalDate createdAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getNickname(), user.getCreatedAt().toLocalDate());
    }
}

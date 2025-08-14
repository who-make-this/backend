package university.likelion.wmt.domain.user.dto.response;

import java.time.LocalDate;

import lombok.Builder;

import university.likelion.wmt.domain.user.entity.User;

@Builder
public record UserInfoResponse(
    String nickname,
    LocalDate createdAt
) {
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
            .nickname(user.getNickname())
            .createdAt(user.getCreatedAt().toLocalDate())
            .build();
    }
}

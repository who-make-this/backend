package university.likelion.wmt.domain.user.dto.request;

public record TokenRefreshRequest(
    String accessToken,
    String refreshToken
) {
}

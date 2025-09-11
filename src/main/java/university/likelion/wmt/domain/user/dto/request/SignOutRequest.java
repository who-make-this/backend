package university.likelion.wmt.domain.user.dto.request;

public record SignOutRequest(
    String accessToken,
    String refreshToken
) {
}

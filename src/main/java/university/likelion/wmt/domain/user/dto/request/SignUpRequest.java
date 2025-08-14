package university.likelion.wmt.domain.user.dto.request;

public record SignUpRequest(
    String username,
    String password,
    String nickname
) {
}

package university.likelion.wmt.domain.user.dto.response;

public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
    public TokenResponse(String accessToken) {
        this(accessToken, "Bearer", 1209600L);
    }
}

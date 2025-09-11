package university.likelion.wmt.domain.user.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long accessTokenExpiresIn,
    long refreshTokenExpiresIn
) {
    public TokenResponse(String accessToken, long accessTokenExpiresIn, String refreshToken, long refreshTokenExpiresIn) {
        this(accessToken, refreshToken, "Bearer", accessTokenExpiresIn, refreshTokenExpiresIn);
    }
}

package university.likelion.wmt.common.auth;

public record Tokens(
    String accessToken,
    Long accessTokenExpiresIn,
    String refreshToken,
    Long refreshTokenExpiresIn
) {
}

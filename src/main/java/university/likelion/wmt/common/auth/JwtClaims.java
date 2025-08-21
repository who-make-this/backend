package university.likelion.wmt.common.auth;

import university.likelion.wmt.domain.user.entity.Role;

public record JwtClaims(
    Long userId,
    Role userRole
) {
}

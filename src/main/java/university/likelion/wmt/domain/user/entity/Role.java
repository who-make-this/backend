package university.likelion.wmt.domain.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Role {
    ROLE_USER("ROLE_USER", "사용자"),
    ROLE_ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}

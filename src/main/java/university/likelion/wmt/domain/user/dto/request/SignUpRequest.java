package university.likelion.wmt.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-z][a-z0-9_-]{4,19}$", message = "영문 소문자, 숫자와 특수기호 -, _만 사용할 수 있으며 영문 소문자로 시작하는 20자 이하의 문자열이어야 합니다.")
    String username,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[\\x20-\\x7E]{8,}$", message = "8자 이상의 알파벳 대소문자, 숫자, 특수문자만 사용할 수 있습니다.")
    String password,

    @NotBlank(message = "닉네임을 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣A-Za-z0-9]+$", message = "한글, 영문 대소문자, 숫자만 사용할 수 있으며 20바이트 이하여야 합니다.")
    String nickname
) {
}

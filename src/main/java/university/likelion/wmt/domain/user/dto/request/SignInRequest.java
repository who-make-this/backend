package university.likelion.wmt.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 4, max = 20)
    String username,

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 255)
    String password
) {
}

package university.likelion.wmt.domain.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.user.dto.response.UserInfoResponse;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        if (userId == null) {
            throw new UserException(UserErrorCode.NEED_AUTHORIZED);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(user);
    }
}

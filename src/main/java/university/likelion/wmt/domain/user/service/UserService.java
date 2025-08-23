package university.likelion.wmt.domain.user.service;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.mission.respository.MissionRepository;
import university.likelion.wmt.domain.report.repository.ReportRepository;
import university.likelion.wmt.domain.user.dto.response.UserInfoResponse;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ReportRepository reportRepository;

    public UserInfoResponse getUserInfo(Long userId) {
        if (userId == null) {
            throw new UserException(UserErrorCode.NEED_AUTHORIZED);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        long clearMissionCount = missionRepository.findByUserAndCompletedTrue(user).size();
        long reportCount = reportRepository.findByUser(user).size();

        Map<String, Long> categoryCounts = missionRepository.findByUserAndCompletedTrue(user).stream()
            .collect(Collectors.groupingBy(Mission::getCategory, Collectors.counting()));
        String userType = categoryCounts.entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("입문자");

        return UserInfoResponse.of(user, userType, clearMissionCount, reportCount);
    }
}

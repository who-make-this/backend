package university.likelion.wmt.domain.mission.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.image.implement.ImageValidator;
import university.likelion.wmt.domain.image.implement.ImageWriter;
import university.likelion.wmt.domain.image.repository.ImageRepository;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.market.exception.MarketErrorCode;
import university.likelion.wmt.domain.market.exception.MarketException;
import university.likelion.wmt.domain.market.repository.MarketRepository;
import university.likelion.wmt.domain.mileage.entity.MileageLogReferenceType;
import university.likelion.wmt.domain.mileage.implement.MileageWriter;
import university.likelion.wmt.domain.mission.dto.response.MissionResponse;
import university.likelion.wmt.domain.mission.dto.response.UserProfileResponse;
import university.likelion.wmt.domain.mission.entity.MasterMission;
import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.mission.entity.MissionFailureReason;
import university.likelion.wmt.domain.mission.exception.MissionErrorCode;
import university.likelion.wmt.domain.mission.exception.MissionException;
import university.likelion.wmt.domain.mission.respository.MasterMissionRepository;
import university.likelion.wmt.domain.mission.respository.MissionFailureReasonRepository;
import university.likelion.wmt.domain.mission.respository.MissionRepository;
import university.likelion.wmt.domain.mission.util.MissionPromptBuilder;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;
    private final MasterMissionRepository masterMissionRepository;
    private final UserRepository userRepository;
    private final ImageValidator imageValidator;
    private final ImageWriter imageWriter;
    private final ImageRepository imageRepository;
    private final MissionGeminiService missionGeminiService;
    private final MileageWriter mileageWriter;
    private final MarketRepository marketRepository;
    private final MissionFailureReasonRepository missionFailureReasonRepository;

    @Value("${hackathon.mode.start-date}")
    private String hackathonStartDate;

    @Value("${hackathon.mode.end-date}")
    private String hackathonEndDate;

    @PostConstruct
    public void init() {
        List<MissionFailureReason> reasons = missionFailureReasonRepository.findAll();
        MissionPromptBuilder.setFailureReasons(reasons);
    }

    private User findUserById(Long userId){
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    // 해커톤 기간인지 확인하는 헬퍼 메서드
    private boolean isDuringHackathon(){
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(hackathonStartDate);
        LocalDate end = LocalDate.parse(hackathonEndDate);
        // 종료일을 포함하도록 isAfter 대신 !isAfter를 사용
        return !today.isBefore(start) && !today.isAfter(end);
    }

    @Transactional
    public MissionResponse authenticateMission(Long missionId, Long userId, MultipartFile imageFile) {
        User user = findUserById(userId);
        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        if (!mission.getUser().getId().equals(user.getId())) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        imageValidator.validateAllowedMime(imageFile);
        String imageUrl = imageWriter.upload(imageFile);
        log.info("업로드 된 이미지 URL: {}", imageUrl);
        String cfName = parseImageIdFromUrl(imageUrl);
        log.info("추출된 cfName: {}", cfName);
        Optional<Image> imageOpt = imageRepository.findByCfName(cfName);
        if (imageOpt.isEmpty()) {
            log.error("Image not found for cfName: {}", cfName);
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        Image image = imageOpt.get();

        String geminiResponse = missionGeminiService.authenticateMission(
            mission.getContent(),
            mission.getCategory(),
            imageFile
        );
        if("ERROR".equals(geminiResponse)){
            throw new MissionException(MissionErrorCode.AI_GENERATION_FAILED);
        }
        boolean isSuccess = geminiResponse.startsWith("YES");

        mission.setCompleted(isSuccess);
        mission.setImage(image);

        if(!isSuccess){
            String failureCode = geminiResponse.substring(geminiResponse.indexOf(":")+1);
            MissionFailureReason failureReason = missionFailureReasonRepository.findByCode(failureCode)
                .orElse(null);
            mission.setFailureReason(failureReason);
        }
        missionRepository.save(mission);

        //미션 완료 시 마일리지 UP (※임시 설정 * 100 포인트)
        if(isSuccess) {
            long earnedPoints = 100L;
            mileageWriter.earn(user.getId(), earnedPoints, LocalDateTime.now().plusYears(1), MileageLogReferenceType.MISSION, mission.getId());
            log.info("마일리지 적립 완료. 사용자 ID: {}", user.getId());
        }

        Optional<Mission> next = missionRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(user);

        long remainingMissions = missionRepository.countByUserAndCompletedFalse(user);
        if (remainingMissions < 5) {
            log.info("미션 재고가 부족합니다 ({}개 남음). 새 미션 10개를 생성합니다.", remainingMissions);
            List<MasterMission> masterMissions = getRandomMasterMissions(10);
            masterMissions.forEach(mm -> {
                Mission newMission = Mission.builder()
                    .user(user)
                    .category(mm.getCategory())
                    .content(mm.getContent())
                    .completed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
                missionRepository.save(newMission);
            });
        }

        if (next.isEmpty()) {
            Optional<Mission> newNext = missionRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(user);
            return toResponse(newNext.orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND)));
        }
        return toResponse(mission);
    }

    private String parseImageIdFromUrl(String imageUrl) {
        String urlWithoutVariant = imageUrl.substring(0, imageUrl.lastIndexOf('/'));
        String cfName = urlWithoutVariant.substring(urlWithoutVariant.lastIndexOf('/') + 1);
        return cfName;
    }
    @Transactional
    public MissionResponse startUserExploration(Long userId, Long marketId) {
        log.info("미션 일괄 생성 시작. (사용자: {}, 시장: {}, 개수: {})", userId, marketId, 10);
        User user = findUserById(userId);

        if (!isDuringHackathon()) {
            LocalDate today = LocalDate.now();
            if (user.getLastExplorationStartDate() != null && user.getLastExplorationStartDate().isEqual(today)) {
                throw new MissionException(MissionErrorCode.ALREADY_STARTED_TODAY);
            }
        }
        user.setLastExplorationStartDate(LocalDate.now());
        userRepository.save(user);

        missionRepository.deleteByUserAndCompletedFalse(user);
        List<MasterMission> masterMissions = getRandomMasterMissions(10);

        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        masterMissions.forEach(mm -> {
            Mission newMission = Mission.builder()
                .user(user)
                .market(market)
                .category(mm.getCategory())
                .content(mm.getContent())
                .missionTitle(mm.getMissionTitle())
                .completed(false)
                .createdAt(LocalDateTime.now())
                .build();
            missionRepository.save(newMission);
        });
        Mission firstMission = missionRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(user)
            .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        return toResponse(firstMission);
    }

    @Transactional
    public MissionResponse refreshAndGetNewMission(Long userId, Long marketId) {
        log.info("미션 새로고침 요청. (사용자: {})", userId);
        User user = findUserById(userId);
        missionRepository.deleteByUserAndCompletedFalse(user);
        List<MasterMission> masterMissions = getRandomMasterMissions(10);

        Market market = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        masterMissions.forEach(mm -> {
            Mission newMission = Mission.builder()
                .user(user)
                .market(market)
                .category(mm.getCategory())
                .content(mm.getContent())
                .missionTitle(mm.getMissionTitle())
                .completed(false)
                .createdAt(LocalDateTime.now())
                .build();
            missionRepository.save(newMission);
        });
        Mission firstMission = missionRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(user)
            .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        return toResponse(firstMission);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        String userType = determineUserType(user);
        Optional<Mission> currentMission = missionRepository.findFirstByUserAndCompletedFalseOrderByCreatedAtAsc(user);
        MissionResponse missionResponse = currentMission.map(this::toResponse).orElse(null);
        return new UserProfileResponse(userId, userType, missionResponse);
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getCompletedMissionsByCategory(Long userId, String category) {
        User user = findUserById(userId);
        List<Mission> completedMissions = missionRepository.findByUserAndCategoryAndCompletedTrue(user, category);
        return completedMissions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private String determineUserType(User user) {
        Map<String, Long> categoryCounts = missionRepository.findByUserAndCompletedTrue(user).stream()
            .collect(Collectors.groupingBy(Mission::getCategory, Collectors.counting()));
        return categoryCounts.entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("입문자");
    }

    private List<MasterMission> getRandomMasterMissions(int count) {
        long totalMissions = masterMissionRepository.count();
        if (totalMissions < count) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        return masterMissionRepository.findRandomMissions(count);
    }

    @Transactional
    public void endUserExploration(Long userId){
        log.info("사용자 탐험 종료 처리. 사용자: {}", userId);
        User user = findUserById(userId);
        missionRepository.deleteByUserAndCompletedFalse(user);
    }

    @Transactional(readOnly = true)
    public long getCompletedMissionCount(Long userId, Long marketId) {
        User user = findUserById(userId);
        Market market = marketRepository.findById(marketId)
            .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        return missionRepository.countByUserAndCompletedTrue(user, market);
    }
    @Transactional(readOnly = true)
    public List<MissionResponse> getMissionsByMarket(Long marketId) {
        List<Mission> missions = missionRepository.findByMarketId(marketId);
        return missions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private MissionResponse toResponse(Mission mission) {
        if (mission == null) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        String failureReason = mission.getFailureReason() != null ? mission.getFailureReason().getReason() : null;
        return new MissionResponse(
            mission.getId(),
            mission.getCategory(),
            mission.getContent(),
            mission.getMissionTitle(),
            mission.isCompleted(),
            mission.getCreatedAt(),
            failureReason
        );
    }
}

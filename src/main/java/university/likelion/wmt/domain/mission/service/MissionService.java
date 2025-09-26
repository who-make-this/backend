package university.likelion.wmt.domain.mission.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Value("${wmt.mission.hackathon.mode.start-date}")
    private String hackathonStartDateStr;

    @Value("${wmt.mission.hackathon.mode.end-date}")
    private String hackathonEndDateStr;

    private static final int MIN_REMAINING_MISSIONS = 5;
    private static final int REPLENISH_MISSION_COUNT = 10;
    private static final long MISSION_REWARD_POINTS = 100L;

    // 캐시된 해커톤 기간(매번 파싱 비용 제거)
    private LocalDate hackathonStart;
    private LocalDate hackathonEnd;

    @PostConstruct
    public void init() {
        MissionPromptBuilder.setFailureReasons(missionFailureReasonRepository.findAll());
        hackathonStart = LocalDate.parse(hackathonStartDateStr);
        hackathonEnd   = LocalDate.parse(hackathonEndDateStr);
    }

    @Transactional
    public MissionResponse authenticateMission(Long missionId, Long userId, MultipartFile imageFile) {
        User user = findUserOrThrow(userId);
        Mission mission = findMissionForUserOrThrow(missionId, user);

        Image image = uploadAndFindImage(imageFile);
        MissionEvaluationResult eval = evaluateMission(mission, image.getImageUrl());

        applyMissionResult(mission, image, eval);
        rewardIfSuccess(user, mission, eval.success());

        // 다음 미션 반환 우선(사용자 경험 일관성).
        Optional<Mission> next = findNextActiveMission(user);
        if (next.isPresent()) return toResponse(next.get());

        // 없으면 재고 보충 후 다시 탐색
        replenishMissionsIfNeeded(user, mission.getMarket());
        return findNextActiveMission(user).map(this::toResponse).orElse(toResponse(mission));
    }

    @Transactional
    public MissionResponse startUserExploration(Long userId, Long marketId) {
        log.info("사용자 탐험 시작. 사용자: {}, 시장: {}", userId, marketId);
        User user = findUserOrThrow(userId);

        if (!isDuringHackathon()) {
            LocalDate today = LocalDate.now();
            if (today.equals(user.getLastExplorationStartDate())) {
                throw new MissionException(MissionErrorCode.ALREADY_STARTED_TODAY);
            }
        }

        archiveActiveMissions(user); // 진행 중 미션 정리
        user.startExploration();
        userRepository.save(user);

        Market market = findMarketOrThrow(marketId);
        Mission mission = createNewMission(user, market, collectExcludedMissionNumbers(user));
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse refreshAndGetNewMission(Long userId, Long marketId) {
        log.info("미션 새로고침 요청. 사용자: {}", userId);
        User user = findUserOrThrow(userId);

        List<Mission> archived = archiveActiveMissions(user);
        if (archived.isEmpty()) throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);

        Market market = findMarketOrThrow(marketId);
        Mission mission = createNewMission(user, market, collectExcludedMissionNumbers(user));
        return toResponse(mission);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserOrThrow(userId);
        String userType = determineUserType(user);
        MissionResponse current = missionRepository
            .findFirstByUserAndCompletedFalseAndExplorationEndedFalseOrderByCreatedAtAsc(user)
            .map(this::toResponse).orElse(null);
        return new UserProfileResponse(userId, userType, current);
    }

    private String determineUserType(User user) {
        Map<String, Long> categoryCounts = missionRepository.findByUserAndCompletedTrue(user).stream()
            .collect(Collectors.groupingBy(Mission::getCategory, Collectors.counting()));
        return categoryCounts.entrySet().stream()
            .max(Comparator.comparingLong(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("입문자");
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getCompletedMissionsByCategory(Long userId, String category) {
        User user = findUserOrThrow(userId);
        return missionRepository.findByUserAndCategoryAndCompletedTrueAndIsExplorationEndedFalse(user, category)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void endUserExploration(Long userId) {
        log.info("사용자 탐험 종료 처리. 사용자: {}", userId);
        User user = findUserOrThrow(userId);

        long done = missionRepository.countByUserAndCompletedTrueAndReportIdNull(user);
        log.info("현재 수행한 미션의 개수는 {}개입니다.", done);
        if (done == 0) throw new MissionException(MissionErrorCode.MISSION_NOT_COMPLETED);

        missionRepository.deleteByUserAndCompletedFalse(user); // 미완료 미션 정리
    }

    @Transactional(readOnly = true)
    public long getCompletedMissionCount(Long userId, Long marketId) {
        User user = findUserOrThrow(userId);
        Market market = findMarketOrThrow(marketId);
        return missionRepository.countByUserAndCompletedTrue(user, market);
    }

    public List<MissionResponse> getMissionsByMarket(Long marketId) {
        return missionRepository.findByMarketId(marketId).stream().map(this::toResponse).toList();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private Market findMarketOrThrow(Long marketId) {
        return marketRepository.findById(marketId)
            .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));
    }

    private boolean isDuringHackathon() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(hackathonStart) && !today.isAfter(hackathonEnd);
    }

    private Mission findMissionForUserOrThrow(Long missionId, User user) {
        Mission mission = missionRepository.findById(missionId)
            .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));
        if (!mission.getUser().getId().equals(user.getId())) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        return mission;
    }

    private List<Mission> archiveActiveMissions(User user) {
        List<Mission> actives = missionRepository.findByUserAndCompletedFalseAndExplorationEndedFalse(user);
        if (actives.isEmpty()) return actives;

        log.debug("{}개의 진행 중인 미션을 탐험에서 제외했습니다. userId={}", actives.size(), user.getId());
        return actives;
    }

    private Set<Integer> collectExcludedMissionNumbers(User user) {
        Set<Integer> excluded = new HashSet<>();
        LocalDate lastStart = user.getLastExplorationStartDate();
        if (lastStart == null) return excluded;

        LocalDateTime since = lastStart.atStartOfDay();

        // 완료 미션 번호
        missionRepository.findByUserAndCompletedTrueAndCreatedAtGreaterThanEqual(user, since)
            .stream().map(Mission::getMissionNumbers).forEach(excluded::add);

        // 최근 진행(미완료) 상위 10개 미션 번호
        missionRepository.findTop10ByUserAndCompletedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(user, since)
            .stream().map(Mission::getMissionNumbers).forEach(excluded::add);

        return excluded;
    }

    private Mission createNewMission(User user, Market market, Set<Integer> excludedNumbers) {
        List<MasterMission> masters = getRandomMasterMissions(1, List.copyOf(excludedNumbers));
        if (masters.isEmpty()) {
            log.warn("새로운 미션을 찾을 수 없습니다. userId={}, 제외 대상: {}", user.getId(), excludedNumbers);
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }
        return missionRepository.save(buildMissionEntity(masters.get(0), user, market, LocalDateTime.now()));
    }

    private Mission buildMissionEntity(MasterMission mm, User user, Market market, LocalDateTime createdAt) {
        return Mission.builder()
            .user(user)
            .market(market)
            .category(mm.getCategory())
            .content(mm.getContent())
            .missionTitle(mm.getMissionTitle())
            .missionNumbers(mm.getMissionNumbers())
            .completed(false)
            .explorationEnded(false)
            .createdAt(createdAt)
            .build();
    }

    private String parseImageIdFromUrl(String imageUrl) {
        String urlWithoutVariant = imageUrl.substring(0, imageUrl.lastIndexOf('/'));
        return urlWithoutVariant.substring(urlWithoutVariant.lastIndexOf('/') + 1);
    }

    private Image uploadAndFindImage(MultipartFile file) {
        imageValidator.validateAllowedMime(file);
        String imageUrl = imageWriter.upload(file);
        log.info("업로드 된 이미지 URL: {}", imageUrl);
        String cfName = parseImageIdFromUrl(imageUrl);
        log.info("추출된 cfName: {}", cfName);

        return imageRepository.findByCfName(cfName).orElseThrow(() -> {
            log.error("Image not found for cfName: {}", cfName);
            return new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        });
    }

    private MissionEvaluationResult evaluateMission(Mission mission, String imageUri) {
        String result = missionGeminiService.authenticateMission(
            mission.getContent(), mission.getCategory(), imageUri);
        if ("ERROR".equals(result)) throw new MissionException(MissionErrorCode.AI_GENERATION_FAILED);

        boolean success = result.startsWith("YES");
        MissionFailureReason reason = success ? null : extractFailureReason(result);
        return new MissionEvaluationResult(success, reason);
    }

    private MissionFailureReason extractFailureReason(String geminiResponse) {
        int idx = geminiResponse.indexOf(':');
        if (idx < 0 || idx + 1 >= geminiResponse.length()) return null;

        String code = geminiResponse.substring(idx + 1);
        return missionFailureReasonRepository.findByCode(code).orElse(null);
    }

    private void applyMissionResult(Mission mission, Image image, MissionEvaluationResult eval) {
        mission.setCompleted(eval.success());
        mission.setImage(image);
        mission.setFailureReason(eval.failureReason());
        missionRepository.save(mission);
    }

    private void rewardIfSuccess(User user, Mission mission, boolean success) {
        if (!success) return;
        mileageWriter.earn(
            user.getId(),
            MISSION_REWARD_POINTS,
            LocalDateTime.now().plusYears(1),
            MileageLogReferenceType.MISSION,
            mission.getId()
        );
        log.info("마일리지 적립 완료. 사용자 ID: {}", user.getId());
    }

    private Optional<Mission> findNextActiveMission(User user) {
        return missionRepository.findFirstByUserAndCompletedFalseAndExplorationEndedFalseOrderByCreatedAtAsc(user);
    }

    private void replenishMissionsIfNeeded(User user, Market market) {
        long remaining = missionRepository.countByUserAndCompletedFalse(user);
        if (remaining >= MIN_REMAINING_MISSIONS) return;

        log.info("미션 재고가 부족합니다 ({}개 남음). 새 미션 {}개를 생성합니다.", remaining, REPLENISH_MISSION_COUNT);

        List<Integer> doneNumbers = missionRepository.findByUserAndCompletedTrue(user)
            .stream().map(Mission::getMissionNumbers).toList();

        List<MasterMission> masters = getRandomMasterMissions(REPLENISH_MISSION_COUNT, doneNumbers);
        if (masters.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<Mission> newMissions = masters.stream()
            .map(mm -> buildMissionEntity(mm, user, market, now))
            .toList();

        missionRepository.saveAll(newMissions);
    }

    private MissionResponse toResponse(Mission mission) {
        if (mission == null) throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);

        String failureReason = mission.getFailureReason() != null ? mission.getFailureReason().getReason() : null;
        String imageUrl = mission.getImage() != null ? mission.getImage().getImageUrl() : null;

        return new MissionResponse(
            mission.getId(),
            mission.getCategory(),
            mission.getContent(),
            mission.getMissionTitle(),
            mission.isCompleted(),
            mission.getCreatedAt(),
            failureReason,
            mission.getMissionNumbers(),
            imageUrl
        );
    }

    private List<MasterMission> getRandomMasterMissions(int count, List<Integer> excludedMissionNumbers) {
        long total = masterMissionRepository.count();

        if (excludedMissionNumbers.isEmpty()) {
            return masterMissionRepository.findRandomMissionsWithoutExclusion(count);
        }
        if (total < count + excludedMissionNumbers.size()) {
            log.warn("새로운 미션을 생성하기 위한 미션이 부족합니다. (총: {}개, 제외 대상: {}개)", total, excludedMissionNumbers.size());
        }
        return masterMissionRepository.findRandomMissions(count, excludedMissionNumbers);
    }

    private record MissionEvaluationResult(boolean success, MissionFailureReason failureReason) {}
}

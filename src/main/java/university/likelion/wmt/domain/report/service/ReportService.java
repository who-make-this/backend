package university.likelion.wmt.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import university.likelion.wmt.domain.image.entity.Image;
import university.likelion.wmt.domain.image.implement.ImageWriter;
import university.likelion.wmt.domain.market.entity.Market;
import university.likelion.wmt.domain.market.exception.MarketErrorCode;
import university.likelion.wmt.domain.market.exception.MarketException;
import university.likelion.wmt.domain.market.repository.MarketRepository;
import university.likelion.wmt.domain.mileage.implement.MileageValidator;
import university.likelion.wmt.domain.mission.entity.Mission;
import university.likelion.wmt.domain.mission.exception.MissionErrorCode;
import university.likelion.wmt.domain.mission.exception.MissionException;
import university.likelion.wmt.domain.mission.respository.MissionRepository;
import university.likelion.wmt.domain.report.dto.response.DiaryResponse;
import university.likelion.wmt.domain.report.dto.response.ReportResponse;
import university.likelion.wmt.domain.report.entity.Report;
import university.likelion.wmt.domain.report.exception.ReportErrorCode;
import university.likelion.wmt.domain.report.exception.ReportException;
import university.likelion.wmt.domain.report.repository.ReportRepository;
import university.likelion.wmt.domain.user.entity.User;
import university.likelion.wmt.domain.user.exception.UserErrorCode;
import university.likelion.wmt.domain.user.exception.UserException;
import university.likelion.wmt.domain.user.repository.UserRepository;
import university.likelion.wmt.domain.mileage.repository.MileageLogRepository;
import university.likelion.wmt.domain.mission.dto.response.CompletedMissionImageResponse;

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
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final DiaryGeminiService diaryGeminiService;
    private final ImageWriter imageWriter;
    private final MarketRepository marketRepository;
    private final MileageLogRepository mileageLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Transactional
    public ReportResponse generateReport(Long userId, String selectedImageCfName, Long marketId) {
        log.info("리포트 생성을 시작합니다. userId: {}, selectedImageCfName: {}, marketId: {}", userId, selectedImageCfName, marketId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없습니다. userId: {}", userId);
                return new UserException(UserErrorCode.USER_NOT_FOUND);
            });

        List<Mission> completedMissions = missionRepository.findByUserAndCompletedTrue(user);
        if (completedMissions.isEmpty()) {
            log.warn("완료된 미션이 없습니다. userId: {}", userId);
            throw new MissionException(MissionErrorCode.MISSION_NOT_FOUND);
        }

        log.info("완료된 미션 목록에 {} 이미지가 포함되어 있는지 확인 중...", selectedImageCfName);
        Optional<Mission> selectedMissionOptional = missionRepository.findByUserAndImageCfName(user, selectedImageCfName);

        log.info("findByUserAndImageCfName 결과: {}", selectedMissionOptional.isPresent() ? "미션 찾음" : "미션 찾지 못함");

        Mission selectedMission = selectedMissionOptional
            .orElseThrow(() -> {
                log.error("사용자 {}의 미션 중 cfName {}에 해당하는 이미지를 찾을 수 없습니다.", userId, selectedImageCfName);
                return new ReportException(ReportErrorCode.IMAGE_NOT_FOUND);
            });

        Image selectedImage = selectedMission.getImage();
        if(selectedImage == null){
            log.error("미션 {}에 연결된 Image 엔티티가 null입니다.", selectedMission.getId());
            throw new ReportException(ReportErrorCode.IMAGE_NOT_FOUND);
        }

        LocalDateTime startDateTime = completedMissions.stream()
            .min(Comparator.comparing(Mission::getCreatedAt))
            .map(Mission::getCreatedAt)
            .orElseThrow(() -> {
                log.error("미션 시작 시간을 찾을 수 없습니다. userId: {}", userId);
                return new RuntimeException("미션 시작 시간을 찾을 수 없습니다.");
            });
        LocalDateTime endDateTime = LocalDateTime.now();

        Integer totalScore = completedMissions.size();

        Long earnedMileageForReport = totalScore.longValue() * 100L;

        Map<String, Long> mileageInfo = getMileageInfo(userId);
        Long remainingMonthlyMileage = mileageInfo.get("remainingMonthlyMileage");

        Map<String, Integer> missionsByCategory = completedMissions.stream()
            .collect(Collectors.groupingBy(Mission::getCategory, Collectors.summingInt(m -> 1)));

        String missionsByCategoryJson;
        try {
            missionsByCategoryJson = objectMapper.writeValueAsString(missionsByCategory);
        } catch (JsonProcessingException e) {
            log.error("미션 카테고리 맵을 JSON으로 변환하는 중 오류가 발생했습니다.", e);
            throw new RuntimeException("미션 카테고리 맵을 JSON으로 변환하는 중 오류가 발생했습니다.", e);
        }

        // marketId를 사용하여 Market 엔티티를 찾고, 리포트 제목을 생성
        Market market = marketRepository.findById(marketId)
            .orElseThrow(() -> new MarketException(MarketErrorCode.MARKET_NOT_FOUND));

        String reportTitle = market.getName() + " 탐험";

        // Report 엔티티 생성 시 market과 reportTitle 저장
        Report report = Report.builder()
            .user(user)
            .market(market)
            .explorationDate(LocalDateTime.now())
            .startTime(startDateTime)
            .endTime(endDateTime)
            .completedMissionsByCategoriesJson(missionsByCategoryJson)
            .totalScore(totalScore)
            .mainImage(selectedImageCfName)
            .reportTitle(reportTitle)
            .build();
        reportRepository.save(report);

        log.info("리포트 {} 저장 완료.", report.getId());

        completedMissions.forEach(mission -> mission.setReportId(report.getId()));
        missionRepository.saveAll(completedMissions);

        log.info("완료된 미션 {}개에 리포트 ID 설정 완료.", completedMissions.size());

        String weatherInfo = diaryGeminiService.getWeatherFromGemini(startDateTime.toLocalDate());
        String journalText = diaryGeminiService.generateJournal(toReportResponse(report, earnedMileageForReport, remainingMonthlyMileage), completedMissions, weatherInfo, selectedImageCfName);
        report.setJournalContent(journalText);
        reportRepository.save(report);

        missionRepository.deleteByUserAndCompletedFalse(user);
        log.info("완료되지 않은 미션 삭제 완료.");

        return toReportResponse(report, earnedMileageForReport, remainingMonthlyMileage);
    }

    public DiaryResponse getDiary(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        return new DiaryResponse(
            report.getExplorationDate(),
            report.getJournalContent()
        );
    }

    @Transactional
    public List<CompletedMissionImageResponse> getCompletedMissionImages(Long userId){
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        log.info("사용자 {}의 완료된 미션 이미지 목록 조회 시작.", userId);

        List<CompletedMissionImageResponse> images = missionRepository.findByUserAndCompletedTrue(user).stream()
                .filter(mission -> mission.getImage() != null)
            .map(mission -> {
                String cfName = mission.getImage().getCfName();
                String imageUrl = imageWriter.createImageUrl(cfName);
                return new CompletedMissionImageResponse(
                    mission.getId(),
                    cfName,
                    imageUrl,
                    mission.getCreatedAt()
                );
            })
            .collect(Collectors.toList());

        log.info("조회된 완료 미션 이미지 수: {}", images.size());
        return images;
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(Long userId){
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        log.info("사용자 {}의 리포트 목록 조회 시작.", userId);

        List<Report> reports = reportRepository.findByUser(user);

        Map<String, Long> mileageInfo = getMileageInfo(userId);
        Long remainingMonthlyMileage = mileageInfo.get("remainingMonthlyMileage");

        log.info("조회된 리포트 수: {}", reports.size());
        return reports.stream()
            .map(report -> {
                List<Mission> reportMissions = missionRepository.findByReportId(report.getId());
                Long earnedMileageForReport = (long) reportMissions.size() * 100L;
                return toReportResponse(report, earnedMileageForReport, remainingMonthlyMileage);
            })
            .collect(Collectors.toList());
    }

    private ReportResponse toReportResponse(Report report, Long earnedMileage, Long remainingMonthlyMileage) {
        Map<String, Integer> completedMissionsByCategories;
        try {
            completedMissionsByCategories = objectMapper.readValue(report.getCompletedMissionsByCategoriesJson(), Map.class);
        } catch (JsonProcessingException e) {
            log.error("JSON 문자열을 미션 카테고리 맵으로 변환하는 중 오류가 발생했습니다. reportId: {}", report.getId(), e);
            throw new RuntimeException("JSON 문자열을 미션 카테고리 맵으로 변환하는 중 오류가 발생했습니다.", e);
        }

        return new ReportResponse(
            report.getId(),
            report.getExplorationDate(),
            report.getStartTime(),
            report.getEndTime(),
            completedMissionsByCategories,
            report.getTotalScore(),
            earnedMileage,
            remainingMonthlyMileage,
            report.getReportTitle(),
            report.getJournalContent()
        );
    }

    private Map<String, Long> getMileageInfo(Long userId) {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        long earnedThisMonth = mileageLogRepository.findMileageSumByUserIdAndRegDateBetween(userId, monthStart, monthEnd);
        Long remainingMonthlyMileage = MileageValidator.getMonthlyEarnCap() - earnedThisMonth;

        return Map.of(
            "earnedThisMonth", earnedThisMonth,
            "remainingMonthlyMileage", remainingMonthlyMileage
        );
    }
}

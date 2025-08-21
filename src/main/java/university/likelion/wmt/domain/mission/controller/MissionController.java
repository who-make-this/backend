package university.likelion.wmt.domain.mission.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import university.likelion.wmt.domain.mission.dto.response.MissionResponse;
import university.likelion.wmt.domain.mission.dto.response.UserProfileResponse;
import university.likelion.wmt.domain.mission.service.MissionService;

import java.util.List;

@Slf4j // 로깅을 위해 Slf4j 어노테이션 추가
@RestController
@RequiredArgsConstructor
@RequestMapping("/missions")
@PreAuthorize("isAuthenticated()") //미션 컨트롤러 모든 api -> 인증 필요
public class MissionController {
    private final MissionService missionService;

    @PostMapping("/start")
    public ResponseEntity<MissionResponse> startMission(
        @AuthenticationPrincipal Long userId,
        @RequestParam("marketId") Long marketId) {
        log.info("startMission() 메서드 호출, 사용자: {}, 시장 ID: {}", userId, marketId);
        try {
            MissionResponse newMission = missionService.startUserExploration(userId,  marketId);
            log.info("새로운 미션이 성공적으로 생성되었습니다.");
            return ResponseEntity.ok(newMission);
        } catch (Exception e) {
            log.error("미션 생성 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/end")
    public ResponseEntity<?> endMission(
        @AuthenticationPrincipal Long userId,
        @RequestParam("marketId") Long marketId){
        long completedMissionCount = missionService.getCompletedMissionCount(userId, marketId);
        if(completedMissionCount == 0){
            return ResponseEntity.badRequest().body("미션을 하나도 완료하지 않았습니다.");
        }
        missionService.endUserExploration(userId);
        return ResponseEntity.ok("탐험이 성공적으로 종료되었습니다.");
    }

    @PostMapping("/authenticate/{missionId}")
    public ResponseEntity<MissionResponse> authenticateMission(
        @PathVariable Long missionId,
        @AuthenticationPrincipal Long userId,
        @RequestParam("imageFile") MultipartFile imageFile
    ){
        MissionResponse response = missionService.authenticateMission(missionId, userId, imageFile);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public MissionResponse refreshMission(
        @AuthenticationPrincipal Long userId,
        @RequestParam Long marketId) {
        return missionService.refreshAndGetNewMission(userId, marketId);
    }


    @GetMapping("/profile")
     public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
         UserProfileResponse userProfile = missionService.getUserProfile(userId);
       return ResponseEntity.ok(userProfile);
    }


    @GetMapping("/completed/{category}")
    public ResponseEntity<List<MissionResponse>> getCompletedMissionsByCategory(
        @AuthenticationPrincipal Long userId,
        @PathVariable String category
    ){
        List<MissionResponse> completedMissions = missionService.getCompletedMissionsByCategory(userId, category);
        return ResponseEntity.ok(completedMissions);
    }
}

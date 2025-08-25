package university.likelion.wmt.domain.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import university.likelion.wmt.domain.mission.dto.response.CompletedMissionImageResponse;
import university.likelion.wmt.domain.report.dto.request.ReportRequest;
import university.likelion.wmt.domain.report.dto.response.ReportResponse;
import university.likelion.wmt.domain.report.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // POST /reports/generate 엔드포인트로 요청을 받습니다.
    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generateReport(@AuthenticationPrincipal Long userId, @RequestBody ReportRequest reportRequest) {
        ReportResponse reportResponse = reportService.generateReport(userId, reportRequest.getSelectedImageUrl(), reportRequest.getMarketId()); // marketId 전달
        return new ResponseEntity<>(reportResponse, HttpStatus.CREATED);
    }

    @GetMapping("/completed-images")
    public ResponseEntity<List<CompletedMissionImageResponse>> getUnreportedMissionImages(@AuthenticationPrincipal Long userId) {
        List<CompletedMissionImageResponse> images = reportService.getUnreportedMissionImages(userId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportResponse>> getMyReports(@AuthenticationPrincipal Long userId) {
        List<ReportResponse> myReports = reportService.getMyReports(userId);
        return ResponseEntity.ok(myReports);
    }
}

package university.likelion.wmt.domain.mileage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.dto.response.BalanceResponse;
import university.likelion.wmt.domain.mileage.service.MileageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mileage")
public class MileageController {
    private final MileageService mileageService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BalanceResponse> remaining(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(mileageService.getBalance(userId));
    }

    // private final MileageWriter mileageWriter;
    //
    // @PostMapping("/earn")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<MileageLogResponse> earn(@Valid @RequestBody EarnRequest req) {
    //     MileageLog log = mileageWriter.earn(req.userId(), req.amount(), req.expiresAt(),
    //         MileageLogReferenceType.valueOf(req.refType()), req.refId());
    //     return ResponseEntity.ok(MileageLogResponse.from(log));
    // }
    //
    // @PostMapping("/use")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<MileageLogResponse> use(@Valid @RequestBody UseRequest req) {
    //     MileageLog log = mileageWriter.use(req.userId(), req.amount(), MileageLogReferenceType.valueOf(req.refType()),
    //         req.refId());
    //     return ResponseEntity.ok(MileageLogResponse.from(log));
    // }
    //
    // public record EarnRequest(
    //     @NotNull Long userId,
    //     @NotNull @Positive Long amount,
    //     LocalDateTime expiresAt,
    //     String refType,
    //     Long refId
    // ) {
    // }
    //
    // public record UseRequest(
    //     @NotNull Long userId,
    //     @NotNull @Positive Long amount,
    //     String refType,
    //     Long refId
    // ) {
    // }
    //
    // public record MileageLogResponse(
    //     Long id,
    //     Long userId,
    //     String type,
    //     Long amount,
    //     LocalDateTime expiresAt,
    //     String refType,
    //     Long refId,
    //     LocalDateTime createdAt
    // ) {
    //     public static MileageLogResponse from(MileageLog log) {
    //         return new MileageLogResponse(log.getId(),
    //             log.getUserId(),
    //             log.getType().name(),
    //             log.getAmount(),
    //             log.getExpiresAt(),
    //             log.getReferenceType().name(),
    //             log.getReferenceId(),
    //             log.getCreatedAt());
    //     }
    // }
}

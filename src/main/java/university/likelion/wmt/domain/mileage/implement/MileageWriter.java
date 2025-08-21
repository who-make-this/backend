package university.likelion.wmt.domain.mileage.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.entity.MileageApply;
import university.likelion.wmt.domain.mileage.entity.MileageLog;
import university.likelion.wmt.domain.mileage.entity.MileageLogReferenceType;
import university.likelion.wmt.domain.mileage.entity.MileageLogType;
import university.likelion.wmt.domain.mileage.repository.MileageApplyRepository;
import university.likelion.wmt.domain.mileage.repository.MileageLogRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageWriter {
    private final MileageLogRepository mileageLogRepository;
    private final MileageApplyRepository mileageApplyRepository;
    private final MileageValidator mileageValidator;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public MileageLog earn(long userId, long amount, LocalDateTime expiresAt, MileageLogReferenceType referenceType,
        Long referenceId) {
        // 마일리지 양이 양수인지 확인합니다.
        mileageValidator.validatePositiveAmount(amount);
        // 한 달에 획득할 수 있는 마일리지 양을 초과하지 않는지 확인합니다.
        mileageValidator.validateWithInMonthlyEarnCap(userId, amount);

        MileageLog earnLog = MileageLog.builder()
            .userId(userId)
            .type(MileageLogType.EARN)
            .amount(amount)
            .expiresAt(expiresAt)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .reversalOf(null)
            .build();
        mileageLogRepository.save(earnLog);

        return earnLog;
    }

    @Transactional
    public MileageLog use(long userId, long amount, MileageLogReferenceType referenceType, Long referenceId) {
        // 마일리지 양이 양수인지 확인합니다.
        mileageValidator.validatePositiveAmount(amount);

        // 사용할 수 있는 마일리지 양 확인합니다.
        LocalDateTime now = LocalDateTime.now();
        long usable = mileageLogRepository.findUsableMileageByUserIdAndRegDateBefore(userId, now);
        mileageValidator.validateUsableBalance(usable, amount);

        // 사용할 수 있는 마일리지 기록 가져옵니다.
        List<MileageLog> creditLogs = mileageLogRepository.findUsableMileageByUserIdAndAmountOrderByExpiresAtWithPessimisticLock(
            userId, amount, now);
        // 마일리지를 사용할 수 있는지 다시 확인합니다.
        long planned = creditLogs.stream()
            .mapToLong(log -> log.getRemaining() == null ? 0L : log.getRemaining())
            .sum();
        mileageValidator.validateUsableBalance(planned, amount);

        MileageLog useLog = MileageLog.builder()
            .userId(userId)
            .type(MileageLogType.USE)
            .amount(-amount)
            .expiresAt(null)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .reversalOf(null)
            .build();
        mileageLogRepository.save(useLog);

        long remainingNeed = amount;
        List<MileageApply> applies = new ArrayList<>();
        for (MileageLog srcCredit : creditLogs) {
            // 마일리지 상세 기록 등록을 위해 마일리지 기록에서 남은 마일리지 양을 계산합니다.
            long remainingInLog =
                srcCredit.getRemaining() == null ? 0L : srcCredit.getRemaining();
            if (remainingInLog <= 0L) {
                continue;
            }

            long piece = Math.min(remainingInLog, remainingNeed);
            MileageApply apply = MileageApply.builder()
                .srcLog(em.getReference(MileageLog.class, srcCredit.getId()))
                .dstLog(useLog)
                .amount(piece)
                .build();
            applies.add(apply);

            remainingNeed -= piece;
            if (remainingNeed == 0L) {
                break;
            }
        }

        mileageApplyRepository.saveAll(applies);
        return useLog;
    }
}

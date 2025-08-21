package university.likelion.wmt.domain.mileage.implement;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.exception.MileageErrorCode;
import university.likelion.wmt.domain.mileage.exception.MileageException;
import university.likelion.wmt.domain.mileage.repository.MileageApplyRepository;
import university.likelion.wmt.domain.mileage.repository.MileageLogRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageValidator {
    private static final long MONTHLY_EARN_CAP = 5_000L;

    private final MileageLogRepository mileageLogRepository;
    private final MileageApplyRepository mileageApplyRepository;

    public static long getMonthlyEarnCap() {
        return MONTHLY_EARN_CAP;
    }

    public void validatePositiveAmount(Long amount) {
        if (amount <= 0L) {
            throw new IllegalArgumentException("적립 마일리지는 양수여야 합니다.");
        }
    }

    public void validateUsableBalance(Long balance, Long requiredAmount) {
        if (balance < requiredAmount) {
            throw new MileageException(MileageErrorCode.USABLE_BALANCE_INSUFFICIENT);
        }
    }

    public void validateWithInMonthlyEarnCap(Long userId, Long amount) {
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime monthEnd = LocalDateTime.now().plusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        long earnedThisMonth = mileageLogRepository.findMileageSumByUserIdAndRegDateBetween(userId, monthStart,
            monthEnd);

        if (earnedThisMonth + amount > MONTHLY_EARN_CAP) {
            throw new MileageException(MileageErrorCode.MONTHLY_EARN_CAP_EXCEEDED);
        }
    }
}

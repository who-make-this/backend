package university.likelion.wmt.domain.mileage.repository;

import static university.likelion.wmt.domain.mileage.entity.QMileageApply.*;
import static university.likelion.wmt.domain.mileage.entity.QMileageLog.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.entity.MileageLog;
import university.likelion.wmt.domain.mileage.entity.MileageLogType;

@Repository
@RequiredArgsConstructor
public class MileageLogRepositoryImpl implements MileageLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MileageLog> findUsableMileageByUserIdAndAmountOrderByExpiresAtWithPessimisticLock(Long userId,
        Long needAmount,
        LocalDateTime now) {

        // 필요한 마일리지가 null이거나 0 이하인 경우 사용할 수 있는 마일리지가 없으므로 빈 리스트를 반환합니다.
        if (needAmount == null || needAmount <= 0L) {
            return List.of();
        }

        //=> 단일 획득 마일리지 기록에서 상세 마일리지 사용 기록에 따라 남은 마일리지를 계산합니다.
        // mileage_log_id와 일치하는 mileage_apply 튜플을 찾아 amount 필드의 값을 모두 더한 후,
        // mileage_log 튜플의 amount에서 mileage_apply 튜플에서 모두 더한 값을 뺄셈합니다.
        NumberExpression<Long> remaining = mileageLog.amount.longValue().subtract(JPAExpressions
            .select(mileageApply.amount.sumLong().coalesce(0L))
            .from(mileageApply)
            .where(mileageApply.srcLog.id.eq(mileageLog.id)));

        //=> 마일리지가 존재하는 단일 획득 마일리지 기록을 찾습니다.
        // 배제 잠금을 실시하여 작업 중 마일리지 적립 또는 사용을 방지합니다.
        // 남은 마일리지가 있는 만료되지 않은 획득 마일리지 기록 튜플을 찾습니다.
        // 만료일이 임박한 마일리지부터 사용하기 위해 만료일, ID 순으로 튜플을 정렬합니다.
        List<Tuple> rows = queryFactory
            .select(mileageLog, remaining)
            .from(mileageLog)
            .where(mileageLog.userId.eq(userId)
                .and(mileageLog.amount.gt(0L))
                .and(mileageLog.type.in(MileageLogType.EARN, MileageLogType.REFUND, MileageLogType.ADJUST))
                .and(mileageLog.expiresAt.isNull().or(mileageLog.expiresAt.gt(now)))
                .and(remaining.gt(0L)))
            .orderBy(mileageLog.expiresAt.asc().nullsLast(), mileageLog.id.asc())
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetch();

        long accumulated = 0L;
        List<MileageLog> picked = new ArrayList<>();
        for (Tuple tuple : rows) {
            MileageLog creditLog = tuple.get(mileageLog);

            // 남은 마일리지를 확인합니다.
            Long remain = Optional.ofNullable(tuple.get(1, Number.class)).map(Number::longValue).orElse(0L);
            if (remain <= 0L) {
                continue;
            }

            // 마일리지가 남아있다면 picked(사용할 수 있는 마일리지 리스트)에 추가합니다.
            assert creditLog != null;
            creditLog.setRemaining(remain);
            picked.add(creditLog);
            accumulated += remain;

            // 필요한 마일리지에 도달하면 작업을 중단합니다.
            if (accumulated >= needAmount) {
                break;
            }
        }

        return picked;
    }

    @Override
    public long findMileageSumByUserIdAndRegDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        //=> 특정 기간 사이에 획득한 마일리지의 양을 계산합니다.
        Long sum = queryFactory
            .select(mileageLog.amount.sumLong())
            .from(mileageLog)
            .where(mileageLog.userId.eq(userId)
                .and(mileageLog.type.eq(MileageLogType.EARN))
                .and(mileageLog.createdAt.goe(start))
                .and(mileageLog.createdAt.lt(end))
            )
            .fetchOne();

        return sum == null ? 0 : sum;
    }

    @Override
    public long findUsableMileageByUserIdAndRegDateBefore(Long userId, LocalDateTime now) {
        //=> 사용할 수 있는 마일리지의 양을 계산합니다.
        Long sumCredit = queryFactory
            .select(mileageLog.amount.sumLong())
            .from(mileageLog)
            .where(mileageLog.userId.eq(userId),
                mileageLog.amount.gt(0L),
                mileageLog.type.in(MileageLogType.EARN, MileageLogType.REFUND, MileageLogType.ADJUST),
                mileageLog.expiresAt.isNull().or(mileageLog.expiresAt.gt(now)))
            .fetchOne();

        //=> 사용할 수 있는 마일리지를 가진 마일리지 기록에서 이미 사용된 마일리지 양을 계산합니다.
        Long sumApplied = queryFactory
            .select(mileageApply.amount.sumLong())
            .from(mileageApply)
            .join(mileageApply.srcLog, mileageLog)
            .where(mileageLog.userId.eq(userId),
                mileageLog.amount.gt(0L),
                mileageLog.type.in(MileageLogType.EARN, MileageLogType.REFUND, MileageLogType.ADJUST),
                mileageLog.expiresAt.isNull().or(mileageLog.expiresAt.gt(now)))
            .fetchOne();

        //=> 실제 사용할 수 있는 마일리지 양을 계산합니다.
        long credit = sumCredit == null ? 0L : sumCredit;
        long applied = sumApplied == null ? 0L : sumApplied;
        long usable = credit - applied;
        return Math.max(0L, usable);
    }
}

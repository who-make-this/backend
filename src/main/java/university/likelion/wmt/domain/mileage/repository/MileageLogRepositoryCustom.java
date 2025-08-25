package university.likelion.wmt.domain.mileage.repository;

import java.time.LocalDateTime;
import java.util.List;

import university.likelion.wmt.domain.mileage.entity.MileageLog;
import university.likelion.wmt.domain.mileage.entity.MileageLogReferenceType;

public interface MileageLogRepositoryCustom {
    List<MileageLog> findUsableMileageByUserIdAndAmountOrderByExpiresAtWithPessimisticLock(Long userId,
        Long needAmount,
        LocalDateTime now);

    long findMileageSumByUserIdAndRegDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    long findMileageSumByUserIdAndRefTypeAndRegDateBetween(Long userId, MileageLogReferenceType type, LocalDateTime start, LocalDateTime end);

    long findUsableMileageByUserIdAndRegDateBefore(Long userId, LocalDateTime now);
}

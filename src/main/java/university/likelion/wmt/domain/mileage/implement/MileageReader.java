package university.likelion.wmt.domain.mileage.implement;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.repository.MileageLogRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageReader {
    private final MileageLogRepository mileageLogRepository;

    public long getBalance(long userId) {
        return mileageLogRepository.findUsableMileageByUserIdAndRegDateBefore(userId, LocalDateTime.now());
    }
}

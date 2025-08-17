package university.likelion.wmt.domain.mileage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.mileage.entity.MileageLog;

public interface MileageLogRepository extends JpaRepository<MileageLog, Long>, MileageLogRepositoryCustom {
    List<MileageLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}

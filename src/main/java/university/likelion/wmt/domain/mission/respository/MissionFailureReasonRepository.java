package university.likelion.wmt.domain.mission.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import university.likelion.wmt.domain.mission.entity.MissionFailureReason;

import java.util.Optional;

public interface MissionFailureReasonRepository extends JpaRepository<MissionFailureReason, Long> {
    Optional<MissionFailureReason> findByCode(String code);
}

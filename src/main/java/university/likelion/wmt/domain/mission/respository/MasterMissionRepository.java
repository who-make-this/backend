package university.likelion.wmt.domain.mission.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import university.likelion.wmt.domain.mission.entity.MasterMission;

import java.util.List;

@Repository
public interface MasterMissionRepository extends JpaRepository<MasterMission, Long> {
    @Query(value = "SELECT * FROM master_mission ORDER BY RAND() LIMIT :count",
        nativeQuery = true) //무작위로 섞기
    List<MasterMission> findRandomMissions(@Param("count") int count);
}

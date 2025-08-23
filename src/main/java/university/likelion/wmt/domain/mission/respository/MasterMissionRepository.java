package university.likelion.wmt.domain.mission.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import university.likelion.wmt.domain.mission.entity.MasterMission;

import java.util.List;

@Repository
public interface MasterMissionRepository extends JpaRepository<MasterMission, Long> {
    @Query(value =
        "SELECT * FROM master_mission " +
            "WHERE mission_numbers" +
            " NOT IN :completedMissionNumbers " +
            "ORDER BY RAND() LIMIT :count",
        nativeQuery = true)
    List<MasterMission> findRandomMissions(
        @Param("count") int count,
        @Param("completedMissionNumbers") List<Integer> completedMissionNumbers
    );
    // 완료된 미션 없을 때 ex) 제일 처음 미션 생성할때
    @Query(value =
    "SELECT * FROM master_mission " +
    "ORDER BY RAND() LIMIT :count",
    nativeQuery = true)
    List<MasterMission> findRandomMissionsWithoutExclusion(
        @Param("count") int count
    );
}

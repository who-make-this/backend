package university.likelion.wmt.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterMission { //미리 만들어둘 100개의 미션을 저장하는 DB테이블의 설계도
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable  = false)
    private Long id;

    @Column(name = "missionTitle", nullable = false, length = 20)
    private String missionTitle;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}

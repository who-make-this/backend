package university.likelion.wmt.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "master_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterMission { //미리 만들어둘 60개의 미션을 저장
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "missionTitle", nullable = false, length = 20)
    private String missionTitle;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "mission_numbers", nullable = false, unique = true)
    private Integer missionNumbers;

    @Builder
    public MasterMission(Long id, String missionTitle,
                         String category, String content,
                         Integer missionNumbers) {
        this.id = id;
        this.missionTitle = missionTitle;
        this.category = category;
        this.content = content;
        this.missionNumbers = missionNumbers;
    }
}


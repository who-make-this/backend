package university.likelion.wmt.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "failure_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionFailureReason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    public MissionFailureReason(String code, String reason){
        this.code = code;
        this.reason = reason;
    }

}

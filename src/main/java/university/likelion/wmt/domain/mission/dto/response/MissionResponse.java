package university.likelion.wmt.domain.mission.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MissionResponse {
    private Long id;
    private String category;
    private String content;
    private String missionTitle;
    private boolean completed;
    private LocalDateTime createdAt;
    private String failureReason;

    public MissionResponse(Long id,
                           String category,
                           String content,
                           String missionTitle,
                           boolean completed,
                           LocalDateTime createdAt,
                           String failureReason) {
        this.id = id;
        this.category = category;
        this.content = content;
        this.missionTitle = missionTitle;
        this.completed = completed;
        this.createdAt = createdAt;
        this.failureReason = failureReason;
    }
}

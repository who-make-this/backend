package university.likelion.wmt.domain.mission.dto.response;

import java.time.LocalDateTime;

public record CompletedMissionImageResponse(
    Long missionId,
    String cfName,
    String imageUrl,
    LocalDateTime completedAt
) {}

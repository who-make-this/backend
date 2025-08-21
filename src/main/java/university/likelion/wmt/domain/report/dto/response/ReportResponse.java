package university.likelion.wmt.domain.report.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record ReportResponse(
    Long id,
    LocalDateTime explorationDate,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Map<String, Integer> completedMissionsByCategories,
    Integer totalScore,
    Long earnedMileage,
    Long remainingMonthlyMileage,
    String reportTitle,
    String journalContent,
    String userType
) {}

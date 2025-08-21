package university.likelion.wmt.domain.report.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryResponse(
    LocalDateTime explorationDate,
    String journalContent
) {}

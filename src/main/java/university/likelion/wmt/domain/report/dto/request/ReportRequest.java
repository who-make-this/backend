package university.likelion.wmt.domain.report.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private String selectedImageCfName;
    private Long marketId; // marketId 필드 추가
}

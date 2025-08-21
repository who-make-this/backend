package university.likelion.wmt.domain.report.exception;

public class ReportException extends RuntimeException{
    private final ReportErrorCode errorCode;
    public ReportException(ReportErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

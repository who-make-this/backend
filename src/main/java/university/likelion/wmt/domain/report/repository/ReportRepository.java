package university.likelion.wmt.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import university.likelion.wmt.domain.report.entity.Report;
import university.likelion.wmt.domain.user.entity.User;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);
    List<Report> findByMarketId(Long marketId);
}

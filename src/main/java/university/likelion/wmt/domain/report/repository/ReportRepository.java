package university.likelion.wmt.domain.report.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.report.entity.Report;
import university.likelion.wmt.domain.user.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserOrderByExplorationDateDesc(User user);

    List<Report> findByMarketId(Long marketId);

    long countByUser(User user);
}

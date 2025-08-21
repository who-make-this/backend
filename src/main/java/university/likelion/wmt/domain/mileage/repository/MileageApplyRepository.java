package university.likelion.wmt.domain.mileage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import university.likelion.wmt.domain.mileage.entity.MileageApply;

public interface MileageApplyRepository extends JpaRepository<MileageApply, Long> {
}
